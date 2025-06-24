package nbc.devmountain.common.config;

import javax.sql.DataSource;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class VectorStoreConfig {

	private final DataSource dataSource;

	@Bean(name = "customVectorStore")
	public VectorStore vectorStore(EmbeddingModel embeddingModel) {
		// JdbcTemplate을 사용하는 올바른 생성자
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		return PgVectorStore.builder(jdbcTemplate, embeddingModel)
				.dimensions(1536)
				.initializeSchema(true)
				.build();
	}

}
