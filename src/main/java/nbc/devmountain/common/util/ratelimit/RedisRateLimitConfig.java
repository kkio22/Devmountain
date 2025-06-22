package nbc.devmountain.common.util.ratelimit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;

@Configuration
public class RedisRateLimitConfig {

	// @Value("${spring.data.redis.port}")
	// private int port;
	// @Value("${spring.data.redis.host}")
	// private String host;
	//
	// private RedisClient redisClient() {
	// 	return RedisClient.create(RedisURI.builder()
	// 		.withHost(host)
	// 		.withPort(port)
	// 		.build());
	// }

	@Bean
	public RedisClient redisClient() {
		// Redis와 연결을 위한 Lettuce 생성
		return RedisClient.create("redis://localhost:6379");
	}

	@Bean
	public StatefulRedisConnection<String, byte[]> redisConnection(RedisClient redisClient) {
		// StringCodec.UTF로 인코딩 / ByteArrayCodec.INSTANCE redis 값을 byte[]로 저장
		RedisCodec<String, byte[]> codec = RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE);
		return redisClient.connect(codec);
	}
}
