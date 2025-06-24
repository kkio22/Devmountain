package nbc.devmountain.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisVectorStoreConfig {

	@Bean
	public RedisVectorStoreConfig redisVectorStoreConfig() {
		return RedisVectorStoreConfig.builder()
			.indexName("lecture_idx")
			.prefix("lecture:")
			.contentFieldName("content")
			.embeddingFieldName("embedding")
			.vectorAlgorithm(Algorithm.HNSW)
			.distanceMetric(DistanceMetric.COSINE)
			.initializeSchema(true)
			.build();
	}

}
