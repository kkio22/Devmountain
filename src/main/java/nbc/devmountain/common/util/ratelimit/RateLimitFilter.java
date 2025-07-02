package nbc.devmountain.common.util.ratelimit;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
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
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.api.StatefulRedisConnection;
import lombok.extern.slf4j.Slf4j;

import nbc.devmountain.common.util.security.CustomUserPrincipal;
import nbc.devmountain.domain.user.model.User;

@Slf4j
public class RateLimitFilter implements Filter {

	// Redis 기반의 ProxyManager를 통해 분산 환경에서도 Bucket을 공유할 수 있도록 설정
	private final ProxyManager<String> proxyManager;
	private final BucketConfiguration config;
	private final BucketConfiguration freeConfig;
	private final BucketConfiguration proConfig;

	// 제한에서 제외할 경로들
	private final List<String> excludedPaths = Arrays.asList(
		"/chatrooms/", // 채팅방 관련 조회 API
		"/api/test"    // 테스트 API
	);

	// 생성자: Redis 연결을 기반으로 ProxyManager를 생성하고 TTL 설정
	public RateLimitFilter(StatefulRedisConnection<String, byte[]> redisConnection) {
			this(
				LettuceBasedProxyManager
					.builderFor(redisConnection)
					.withExpirationStrategy(ExpirationAfterWriteStrategy.fixedTimeToLive(Duration.ofHours(2)))
					.build()
			);
		}

	// 생성자: ProxyManager를 직접 주입받는 생성자
	public RateLimitFilter(ProxyManager<String> proxyManager) {
			this.proxyManager = proxyManager;
			// Free: 1시간 10개
			this.freeConfig = BucketConfiguration.builder()
				// intervally: 전체 기간이 경과할 때까지 기다린 후 전체 토큰을 재생성(현재 설정: 1시간마다 10개 토큰)
				.addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofHours(1))))
				.build();
			// Pro: 1시간 20개
			this.proConfig = BucketConfiguration.builder()
				.addLimit(Bandwidth.classic(20, Refill.intervally(20, Duration.ofHours(1))))
				.build();
			this.config = freeConfig;
		}

	// IP별로 요청 수 제한 처리
	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
		throws IOException, ServletException {

		// HTTP 요청/응답 객체로 캐스팅
		HttpServletRequest request = (HttpServletRequest)servletRequest;
		String requestURI = request.getRequestURI();
		String method = request.getMethod();

		// POST 요청이 아니면 Rate Limit 적용하지 않음
		if (!"POST".equals(method)) {
			filterChain.doFilter(servletRequest, servletResponse);
			return;
		}

		// POST 요청 중에서도 채팅 메시지 전송에만 Rate Limit 적용
		if (!requestURI.matches("/chatrooms/\\d+/messages")) {
			filterChain.doFilter(servletRequest, servletResponse);
			return;
		}

		String ipKey = request.getRemoteAddr(); // 클라이언트 IP를 key로 사용

		// 인증된 사용자의 멤버십 등급 확인
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
		// 해당 IP에 대한 Bucket을 Redis에서 가져오거나 새로 생성
		final BucketConfiguration finalConfigToUse = configToUse;
		Supplier<BucketConfiguration> configSupplier = () -> finalConfigToUse;
		Bucket bucket = proxyManager.builder().build(ipKey, configSupplier);

		log.info("RateLimitFilter: ipKey={}, uri={}, method={}, tokens left={}", ipKey, requestURI, method, bucket.getAvailableTokens());

		if (bucket.tryConsume(1)) {
			// 요청 허용된 경우 필터 체인을 통해 다음 필터로 요청 전달
			filterChain.doFilter(servletRequest, servletResponse);
		} else {
			// 요청 제한 초과 시 429 Too Many Requests 응답 전송
			HttpServletResponse res = (HttpServletResponse)servletResponse;
			res.setStatus(429);
			// 1시간 이후 재시도 요청
			res.setHeader("Retry-After", "3600");
			res.setCharacterEncoding("UTF-8");
			res.setContentType("application/json; charset=UTF-8");
			res.getWriter().write("{\"message\":\"요청이 너무 많습니다. 잠시 후 다시 시도해주세요.\"}");
			log.warn("Rate limit exceeded for IP: {} - Chat message", ipKey);
		}
	}

	// 제외할 경로인지 확인하는 메서드
	private boolean isExcludedPath(String requestURI) {
		return excludedPaths.stream().anyMatch(requestURI::startsWith);
	}
}
