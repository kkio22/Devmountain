package nbc.devmountain.common.util.ratelimit;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import java.time.Duration;

@Configuration
public class FilterConfig {

	public FilterConfig() {
		System.out.println("=== FilterConfig 생성됨 ===");
	}

	@Bean
	public FilterRegistrationBean<RateLimitFilter> rateLimitFilter(
		ProxyManager<String> proxyManager,
		StatefulRedisConnection<String, byte[]> redisConnection
	) {
		FilterRegistrationBean<RateLimitFilter> registrationBean = new FilterRegistrationBean<>();
		registrationBean.setFilter(new RateLimitFilter(proxyManager, redisConnection));
		registrationBean.addUrlPatterns("/*"); // 모든 HTTP 요청에 적용
		registrationBean.setOrder(1); // 필터 순서
		return registrationBean;
	}

	@Bean
	public ProxyManager<String> proxyManager(StatefulRedisConnection<String, byte[]> redisConnection) {
		return LettuceBasedProxyManager
			.builderFor(redisConnection)
			.withExpirationStrategy(ExpirationAfterWriteStrategy.fixedTimeToLive(Duration.ofHours(2)))
			.build();
	}

	@Bean
	public StatefulRedisConnection<String, String> redisStringConnection(RedisClient redisClient) {
		return redisClient.connect(); // 기본 StringCodec 사용
	}
}
