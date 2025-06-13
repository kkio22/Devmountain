package nbc.devmountain.common.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.devmountain.domain.ai.service.RagService;
import nbc.devmountain.domain.lecture.repository.LectureRepository;
import nbc.devmountain.domain.lecture.service.EmbeddingService;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class VectorStoreConfig {

	private final LectureRepository lectureRepository;
	private final DataSource dataSource;

	@Bean(name = "customVectorStore")
	public VectorStore vectorStore(EmbeddingModel embeddingModel) {
		// JdbcTemplate을 사용하는 올바른 생성자
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		return new PgVectorStore(jdbcTemplate, embeddingModel, 1536);
	}

	@Bean
	public ApplicationRunner vectorStoreInitializer(
		RagService ragService,
		EmbeddingService embeddingService) {
		return args -> {
			try {
				log.info("=== 벡터 스토어 초기화 시작 ===");

				long totalLectures = lectureRepository.count();
				log.info("전체 강의 수: {}", totalLectures);
				if (totalLectures == 0) {
					log.warn("강의 데이터가 없습니다. 크롤링을 먼저 시도하세요.");
					return;
				}

				long notEmbeddedCount = lectureRepository.findByIsEmbeddedFalse().size();
				log.info("임베딩 필요한 강의 수: {}", notEmbeddedCount);

				if (notEmbeddedCount > 0) {
					// 1. 임베딩 생성
					log.info("1단계: 임베딩 생성 중...");
					embeddingService.embedLecture();
				}

				// 2. 벡터 스토어에 저장
				log.info("2단계: 벡터 스토어에 저장 중...");
				ragService.saveEmbeddedLecturesToVectorStore();

				log.info("=== 벡터 스토어 초기화 완료 ===");
			} catch (Exception e) {
				log.error("초기화 중 오류 발생: {}", e.getMessage(), e);

			}
		};
	}
}
