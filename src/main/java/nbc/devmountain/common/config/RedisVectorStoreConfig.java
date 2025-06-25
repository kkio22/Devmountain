package nbc.devmountain.common.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPooled;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class RedisVectorStoreConfig {

	@Value("${spring.redis.vectorstore.host}")
	private String host;

	@Value("${spring.redis.vectorstore.port}")
	private int port;

	@Value("${spring.redis.vectorstore.password}")
	private String password;

	@Bean
	public JedisPooled jedisPooled() {
		return new JedisPooled(host, port, "default", password);
	}

	@Bean
	public RedisVectorStore redisVectorStore(JedisPooled jedisPooled, EmbeddingModel embeddingModel) {

		return RedisVectorStore.builder(jedisPooled, embeddingModel)
			.initializeSchema(true)
			.indexName("query_idx")
			.prefix("query")
			.build();

	}

}
