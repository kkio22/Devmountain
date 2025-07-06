package nbc.devmountain.common.util.ratelimit;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import io.lettuce.core.ScriptOutputType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.extern.slf4j.Slf4j;

import io.lettuce.core.api.StatefulRedisConnection;
import nbc.devmountain.common.util.security.CustomUserPrincipal;
import nbc.devmountain.domain.user.model.User;

import java.nio.charset.StandardCharsets;

@Slf4j
public class RateLimitFilter implements Filter {

	private final ProxyManager<String> proxyManager;
	private final BucketConfiguration freeConfig;
	private final BucketConfiguration proConfig;
	private final BucketConfiguration config;
	private final StatefulRedisConnection<String, byte[]> redisConnection;

	private final List<String> excludedPaths = Arrays.asList(
		// "/chatrooms/", // 채팅방 관련 조회 API
		"/api/test"    // 테스트 API
	);

	// 모든 필드를 한 번에 초기화하는 생성자
	public RateLimitFilter(
		ProxyManager<String> proxyManager,
		StatefulRedisConnection<String, byte[]> redisConnection
	) {
		this.proxyManager = proxyManager;
		this.freeConfig = BucketConfiguration.builder()
			.addLimit(Bandwidth.classic(10000, Refill.intervally(10000, Duration.ofHours(1))))
			.build();
		this.proConfig = BucketConfiguration.builder()
			.addLimit(Bandwidth.classic(20, Refill.intervally(20, Duration.ofHours(1))))
			.build();
		this.config = freeConfig;
		this.redisConnection = redisConnection;
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
		throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest)servletRequest;
		String requestURI = request.getRequestURI();
		String method = request.getMethod();

		log.info("RateLimitFilter 진입: method={}, uri={}", method, requestURI);

		if (!"POST".equals(method)) {
			filterChain.doFilter(servletRequest, servletResponse);
			return;
		}

		if (!requestURI.matches("/chatrooms/\\d+/messages")) {
			filterChain.doFilter(servletRequest, servletResponse);
			return;
		}

		String ipKey = request.getRemoteAddr();

		// 1. IP별 버킷
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		BucketConfiguration configToUse = freeConfig;
		if (authentication != null && authentication.isAuthenticated()
			&& authentication.getPrincipal() instanceof CustomUserPrincipal userPrincipal) {
			User.MembershipLevel level = userPrincipal.getMembershipLevel();
			if (level == User.MembershipLevel.PRO) {
				configToUse = proConfig;
			} else {
				configToUse = freeConfig;
			}
		}
		final BucketConfiguration finalConfigToUse = configToUse;
		Supplier<BucketConfiguration> configSupplier = () -> finalConfigToUse;
		Bucket ipBucket = proxyManager.builder().build(ipKey, configSupplier);

		// 2. 글로벌 리밋: 1초에 5회
		String globalKey = "global_rate_limit";
		long globalLimit = 100;
		long globalWindowSeconds = 1; // 1초
		var sync = redisConnection.sync();
		log.info("Redis connection info: {}", redisConnection.getOptions());
		log.info("Redis connection object: {}", redisConnection);
		try {
			String script = "local v = redis.call('incr', KEYS[1]); if v == 1 then redis.call('expire', KEYS[1], ARGV[1]); end; return v;";
			String[] keys = new String[] { globalKey };
			byte[] arg = String.valueOf(globalWindowSeconds).getBytes(StandardCharsets.UTF_8);
			Long count = (Long) sync.eval(script, ScriptOutputType.INTEGER, keys, arg);
			log.info("=== 글로벌 리밋 카운트: {} ===", count);
			if (count > globalLimit) {
				HttpServletResponse res = (HttpServletResponse)servletResponse;
				res.setStatus(429);
				res.setHeader("Retry-After", "3600");
				res.setCharacterEncoding("UTF-8");
				res.setContentType("application/json; charset=UTF-8");
				res.getWriter().write("글로벌 요청이 너무 많습니다. 잠시 후 다시 시도해주세요.");
				log.warn("Global rate limit exceeded: count={}", count);
				return;
			}
		} catch (Exception e) {
			log.error("글로벌 리밋 Redis 오류", e);
		}

		// 3. 둘 다 통과해야 허용
		if (ipBucket.tryConsume(1)) {
			filterChain.doFilter(servletRequest, servletResponse);
		} else {
			HttpServletResponse res = (HttpServletResponse)servletResponse;
			res.setStatus(429);
			res.setHeader("Retry-After", "3600");
			res.setCharacterEncoding("UTF-8");
			res.setContentType("application/json; charset=UTF-8");
			res.getWriter().write("{\"message\":\"요청이 너무 많습니다. 잠시 후 다시 시도해주세요.\"}");
			log.warn("Rate limit exceeded for IP: {}", ipKey);
		}
	}

	private boolean isExcludedPath(String requestURI) {
		return excludedPaths.stream().anyMatch(requestURI::startsWith);
	}
}
