package nbc.devmountain.common.util.ratelimit;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Supplier;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.api.StatefulRedisConnection;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RateLimitFilter implements Filter {

	// Redis 기반의 ProxyManager를 통해 분산 환경에서도 Bucket을 공유할 수 있도록 설정
	private final ProxyManager<String> proxyManager;
	private final BucketConfiguration config;

	// 생성자: Redis 연결을 기반으로 ProxyManager를 생성하고 TTL 설정
	public RateLimitFilter(StatefulRedisConnection<String, byte[]> redisConnection) {
		this.proxyManager = LettuceBasedProxyManager
			.builderFor(redisConnection)
			// fixedTimeToLive: 고정된 시간 이후 만료(현재 설정: 5분)
			.withExpirationStrategy(ExpirationAfterWriteStrategy.fixedTimeToLive(Duration.ofMinutes(5)))
			.build();

		// 요청 제한 설정
		this.config = BucketConfiguration.builder()
		// intervally: 전체 기간이 경과할 때까지 기다린 후 전체 토큰을 재생성(현재 설정: 1분마다 10개 토큰)
			.addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1))))
			.build();
	}

	// IP별로 요청 수 제한 처리
	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
		throws IOException, ServletException {

		// HTTP 요청/응답 객체로 캐스팅
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		String ipKey = request.getRemoteAddr(); // 클라이언트 IP를 key로 사용

		// 동일한 구성의 버킷을 생성하기 위한 Supplier 정의
		Supplier<BucketConfiguration> configSupplier = () -> config;

		// 해당 IP에 대한 Bucket을 Redis에서 가져오거나 새로 생성
		Bucket bucket = proxyManager.builder().build(ipKey, configSupplier);

		if (bucket.tryConsume(1)) {
			// 요청 허용된 경우 필터 체인을 통해 다음 필터로 요청 전달
			filterChain.doFilter(servletRequest, servletResponse);
			// 요청 제한 초과 시 429 Too Many Requests 응답 전송
		} else {
			HttpServletResponse res = (HttpServletResponse) servletResponse;
			res.setStatus(429);
			res.getWriter().write("요청이 너무 많습니다. 잠시 후 다시 시도해주세요.");
			log.warn("Rate limit exceeded for IP: {}", ipKey);
		}
	}
}
