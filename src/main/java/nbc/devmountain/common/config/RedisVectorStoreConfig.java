package nbc.devmountain.common.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.RedisVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPooled;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class RedisVectorStoreConfig {

	@Bean
	public JedisPooled jedisPooled() {
		return new JedisPooled("localhost", 6380, "default", "mypassword");
	}

	@Bean
	public RedisVectorStore redisVectorStore(JedisPooled jedisPooled, EmbeddingModel embeddingModel) {
		RedisVectorStore.RedisVectorStoreConfig config = RedisVectorStore.RedisVectorStoreConfig.builder()
			.withIndexName("query_idx")
			.withPrefix("query")
			.build();
		return new RedisVectorStore(config, embeddingModel, jedisPooled, true);

	}
}
