package nbc.devmountain.common.config;

import org.springframework.ai.document.Document;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

	/*
	이 메서드의 반환 객체를 spring bean으로 등록한다는 의미
	 */
	@Bean
	public Job embeddingJob(Step embeddingStep) {
		return new JobBuilder("embeddingJob", jobRepository)
			.start(embeddingStep)
			.build();
	}

	@Bean
	public Step embeddingStep() {
		return new StepBuilder("embeddingStep", jobRepository)
			.<Lecture, Document>chunk(500)
			.reader(lectureReader())
			.processor(lectureProcessor())
			.writer(lectureWriter())
			.faultTolerant()
			.retryLimit(2)
			.retry(BatchException.class)
			.backOffPolicy(new ExponentialBackOffPolicy())
			.skipLimit(100)
			.skip(BatchException.class)
			.listener(new SkipListener<Lecture, Document>() {
				@Override
				public void onSkipInProcess(Lecture item, Throwable t) {
					log.warn("스킵된 강의 id: {}, 이유: {}", item.getLectureId(), t.getMessage());
				}
			})
			.build();
	}

}
