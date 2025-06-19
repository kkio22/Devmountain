package nbc.devmountain.common.config;

import org.springframework.ai.document.Document;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.devmountain.domain.lecture.batch.InflearnApiReader;
import nbc.devmountain.domain.lecture.client.LectureClient;
import nbc.devmountain.domain.lecture.dto.InflearnResponse;
import nbc.devmountain.domain.lecture.exception.BatchException;
import nbc.devmountain.domain.lecture.model.Lecture;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
@Slf4j
public class BatchJobConfig {
	/*
	batch 실행 순서와 로직 흐름 정의
	 */
	private final JobRepository jobRepository; // Job의 실행 상태를 기록하고 관리하는 곳
	private final LectureClient lectureClient;


	/*
	이 메서드의 반환 객체를 spring bean으로 등록한다는 의미
	 */
	@Bean
	public Job lectureCrawlingJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new JobBuilder("crawling", jobRepository)
			.start(saveCrawledLectureStep(transactionManager))
			.build();
	}

	@Bean
	public Job lectureEmbeddingJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new JobBuilder("embedding", jobRepository)
			.start(EmbeddingLectureStep(transactionManager))
			.build();
	}

	@Bean
	public Step saveCrawledLectureStep(PlatformTransactionManager transactionManager) {
		return new StepBuilder("crawlingLecture", jobRepository)
			.<InflearnResponse, Lecture>chunk(40, transactionManager)
			.reader(inflearnApiReader) //외부 API 호출해서 InflearnResponse 읽기
			.processor() // InfleanRepository -> Lecture로 변환
			.writer() // Lecture 엔티티에 저장
			.faultTolerant()
			.retryLimit(3)
			.retry(BatchException.class)
			.backOffPolicy(new ExponentialBackOffPolicy())
			.skipLimit(5)
			.skip(BatchException.class)
			.build();

	}

	@Bean
	@StepScope
	public InflearnApiReader inflearnApiReader(LectureClient lectureClient){
		return new InflearnApiReader(lectureClient);
	}



	@Bean
	public Step EmbeddingLectureStep(PlatformTransactionManager transactionManager) {
		return new StepBuilder("embeddingLecture", jobRepository)
			.<InflearnResponse, Lecture>chunk(500, transactionManager)
			.reader() //
			.processor() //
			.writer() //
			.faultTolerant()
			.retryLimit(3)
			.retry(BatchException.class)
			.backOffPolicy(new ExponentialBackOffPolicy())
			.skipLimit(100)
			.skip(BatchException.class)
			.build();

	}

}
