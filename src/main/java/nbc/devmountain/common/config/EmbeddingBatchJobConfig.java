package nbc.devmountain.common.config;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.devmountain.domain.lecture.service.batch.embedding.EmbeddingJobResultListener;
import nbc.devmountain.domain.lecture.service.batch.embedding.EmbeddingProcessor;
import nbc.devmountain.domain.lecture.service.batch.embedding.EmbeddingReader;
import nbc.devmountain.domain.lecture.service.batch.embedding.EmbeddingWriter;
import nbc.devmountain.domain.lecture.model.Lecture;
import nbc.devmountain.domain.lecture.repository.LectureRepository;
import nbc.devmountain.domain.lecture.repository.LectureSkillTagRepository;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class EmbeddingBatchJobConfig {

	private final JobRepository jobRepository;
	private final EmbeddingReader embeddingReader;
	private final EmbeddingProcessor embeddingProcessor;
	private final EmbeddingWriter embeddingWriter;
	private final JdbcTemplate jdbcTemplate;
	private final EmbeddingJobResultListener embeddingJobResultListener;

	@Bean
	public Job lectureEmbeddingJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new JobBuilder("lectureEmbeddingJob", jobRepository)
			.start(clearVectorStore(transactionManager))
			.next(saveEmbeddingLectureStep(transactionManager))
			.listener(embeddingJobResultListener)
			.build();
	}

	@Bean
	public Step saveEmbeddingLectureStep(PlatformTransactionManager transactionManager) {
		return new StepBuilder("saveEmbeddingLectureStep", jobRepository)
			.<Lecture, Document>chunk(500, transactionManager)
			.reader(embeddingReader)
			.processor(embeddingProcessor)
			.writer(embeddingWriter)
			.build();

	}

	@Bean
	public Step clearVectorStore(PlatformTransactionManager transactionManager){
		return new StepBuilder("clearVectorStore", jobRepository)
			.tasklet((contribution, chunkContext) -> {
				jdbcTemplate.execute("TRUNCATE TABLE vector_store");
				log.info("vector_store 테이블 초기화 완료");
				return RepeatStatus.FINISHED;
			}, transactionManager)
			.build();
	}

	@Bean
	@StepScope
	public EmbeddingReader embeddingReader(LectureRepository lectureRepository){
		return new EmbeddingReader(lectureRepository);
	}


	@Bean
	@StepScope
	public EmbeddingProcessor embeddingProcessor(LectureSkillTagRepository lectureSkillTagRepository){
		return new EmbeddingProcessor(lectureSkillTagRepository);
	}


	@Bean
	@StepScope
	public EmbeddingWriter embeddingWriter(VectorStore vectorStore){
		return new EmbeddingWriter(vectorStore);
	}

}
