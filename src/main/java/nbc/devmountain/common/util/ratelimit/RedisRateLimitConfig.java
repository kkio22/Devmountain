package nbc.devmountain.common.util.ratelimit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;

@Configuration
public class RedisRateLimitConfig {

	@Value("${ratelimit.redis.host:localhost}")
	private String redisHost;

	@Value("${ratelimit.redis.port:6381}")
	private int redisPort;

	@Bean
	public RedisClient redisClient() {
		String url = String.format("redis://%s:%d", redisHost, redisPort);
		return RedisClient.create(url);
	}

	@Bean
	public StatefulRedisConnection<String, byte[]> redisConnection(RedisClient redisClient) {
		// StringCodec.UTF로 인코딩 / ByteArrayCodec.INSTANCE redis 값을 byte[]로 저장
		RedisCodec<String, byte[]> codec = RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE);
		return redisClient.connect(codec);
	}
}
