package nbc.devmountain.common.config;

import java.util.List;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.devmountain.domain.lecture.batch.embedding.EmbeddingReader;
import nbc.devmountain.domain.lecture.model.Lecture;
import nbc.devmountain.domain.lecture.repository.LectureRepository;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class EmbeddingBatchJobConfig {

	private final JobRepository jobRepository;
	private final EmbeddingReader embeddingReader;

	@Bean
	public Job lectureEmbeddingJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new JobBuilder("lectureEmbeddingJob", jobRepository)
			.start(clearVectorStore(transactionManager))
			.next(saveEmbeddingLectureStep(transactionManager))
			.build();
	}

	@Bean
	public Step saveEmbeddingLectureStep(PlatformTransactionManager transactionManager) {
		return new StepBuilder("saveEmbeddingLectureStep", jobRepository)
			.<List<Lecture>, VectorStore>chunk(500, transactionManager)
			.reader(embeddingReader)
			.processor()
			.writer()
			.faultTolerant()
			.retryLimit(3)
			.retry(Exception.class)
			.skipLimit(3)
			.skip(Exception.class)
			.build();

	}

	@Bean
	public Step clearVectorStore(PlatformTransactionManager transactionManager){
		return new StepBuilder("clearVectorStore", jobRepository)
			.tasklet()
	}

	@Bean
	@StepScope
	public EmbeddingReader embeddingReader(LectureRepository lectureRepository){
		return new EmbeddingReader(lectureRepository);
	}

}
