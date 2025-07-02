package nbc.devmountain.common.util.ratelimit;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.common.config.RedisRateLimitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;

@RequiredArgsConstructor
@Configuration
public class RedisRateLimitConfig {

	private final RedisRateLimitProperties properties;
	private static final Logger log = LoggerFactory.getLogger(RedisRateLimitConfig.class);

	@Bean
	public RedisClient redisClient(
		@Value("${ratelimit.redis.host}") String host,
		@Value("${ratelimit.redis.port}") int port
	) {
		String url = String.format("redis://%s:%d", host, port);
		log.info("Creating RedisClient with url: {}", url);
		return RedisClient.create(url);
	}

	@Bean
	public StatefulRedisConnection<String, byte[]> redisConnection(RedisClient redisClient) {
		// StringCodec.UTF로 인코딩 / ByteArrayCodec.INSTANCE redis 값을 byte[]로 저장
		RedisCodec<String, byte[]> codec = RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE);
		return redisClient.connect(codec);
	}
}
