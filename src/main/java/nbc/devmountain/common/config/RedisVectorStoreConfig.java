package nbc.devmountain.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisVectorStoreConfig {
	
	@Bean
	public RedisVectorStoreConfig redisVectorStoreConfig() {
		return RedisVectorStoreConfig.builder()
			.host("localhost")
			.port(6379)
			.indexName("doc_idx")
			.prefix("doc:")
			.dimension(1536)
			.vectorField("embedding")
			.textField("content")
			.distanceMetric(DistanceMetric.COSINE)
			.algorithm(Algorithm.HNSW)
			.initializeSchema(true)
			.build();
	}

}
