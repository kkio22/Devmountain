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

	private final ProxyManager<String> proxyManager;
	private final BucketConfiguration config;

	public RateLimitFilter(StatefulRedisConnection<String, byte[]> redisConnection) {
		this.proxyManager = LettuceBasedProxyManager
			.builderFor(redisConnection)
			.withExpirationStrategy(ExpirationAfterWriteStrategy.fixedTimeToLive(Duration.ofMinutes(5)))
			.build();

		this.config = BucketConfiguration.builder()
			.addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1))))
			.build();
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
		throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) servletRequest;
		String ipKey = request.getRemoteAddr();

		Supplier<BucketConfiguration> configSupplier = () -> config;

		Bucket bucket = proxyManager.builder().build(ipKey, configSupplier);

		if (bucket.tryConsume(1)) {
			filterChain.doFilter(servletRequest, servletResponse);
		} else {
			HttpServletResponse res = (HttpServletResponse) servletResponse;
			res.setStatus(429);
			res.getWriter().write("요청이 너무 많습니다. 잠시 후 다시 시도해주세요.");
			log.warn("Rate limit exceeded for IP: {}", ipKey);
		}
	}
}
