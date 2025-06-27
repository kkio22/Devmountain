package nbc.devmountain.common.util.ratelimit;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.common.config.RedisRateLimitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;

@RequiredArgsConstructor
@Configuration
public class RedisRateLimitConfig {

	private final RedisRateLimitProperties properties;

	@Bean
	public RedisClient redisClient() {
		String url = String.format("redis://%s:%d", properties.getHost(), properties.getPort());
		System.out.println(url);
		return RedisClient.create(url);
	}

	@Bean
	public StatefulRedisConnection<String, byte[]> redisConnection(RedisClient redisClient) {
		// StringCodec.UTF로 인코딩 / ByteArrayCodec.INSTANCE redis 값을 byte[]로 저장
		RedisCodec<String, byte[]> codec = RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE);
		return redisClient.connect(codec);
	}
}
