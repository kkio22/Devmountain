package nbc.devmountain.common.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.devmountain.domain.lecture.batch.crawling.InflearnApiProcessor;
import nbc.devmountain.domain.lecture.batch.crawling.InflearnApiReader;
import nbc.devmountain.domain.lecture.batch.crawling.InflearnApiWriter;
import nbc.devmountain.domain.lecture.client.LectureClient;
import nbc.devmountain.domain.lecture.dto.InflearnResponse;
import nbc.devmountain.domain.lecture.dto.LectureWithSkillTag;
import nbc.devmountain.domain.lecture.repository.LectureRepository;
import nbc.devmountain.domain.lecture.repository.LectureSkillTagRepository;
import nbc.devmountain.domain.lecture.repository.SkillTagRepository;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class InflearnBatchJobConfig {
	/*
	batch 실행 순서와 로직 흐름 정의
	 */
	private final JobRepository jobRepository; // Job의 실행 상태를 기록하고 관리하는 곳
	private final LectureRepository lectureRepository;
	private final InflearnApiReader inflearnApiReader;
	private final InflearnApiProcessor inflearnApiProcessor;
	private final InflearnApiWriter inflearnApiWriter;

	/*
	이 메서드의 반환 객체를 spring bean으로 등록한다는 의미
	 */
	@Bean
	public Job lectureCrawlingJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
		return new JobBuilder("lectureCrawlingJob", jobRepository)
			.start(saveCrawledLectureStep(transactionManager))
			.next(deleteOldLectureStep(transactionManager))
			.build();
	}

	@Bean
	public Step saveCrawledLectureStep(PlatformTransactionManager transactionManager) {
		return new StepBuilder("saveCrawledLectureStep", jobRepository)
			.<InflearnResponse, List<LectureWithSkillTag>>chunk(1, transactionManager)
			.reader(inflearnApiReader) //외부 API 호출해서 InflearnResponse 읽기
			.processor(inflearnApiProcessor) // InfleanRepository -> Lecture로 변환
			.writer(inflearnApiWriter) // Lecture 엔티티에 저장
			.faultTolerant()
			.retryLimit(3)
			.retry(Exception.class)
			.skipLimit(10)
			.skip(Exception.class)
			.build();

	}

	@Bean
	public Step deleteOldLectureStep(PlatformTransactionManager transactionManager) {
		return new StepBuilder("deleteOldLectureStep", jobRepository)
			.tasklet((contribution, chunkContext) -> {
				LocalDateTime startDate = LocalDate.now().atStartOfDay();
				lectureRepository.deleteByCrawledAtBefore(startDate);
				log.info("오래된 강의 삭제");
				return RepeatStatus.FINISHED;
			}, transactionManager)
			.build();
	}

	@Bean
	@StepScope
	/*
	Step이 실행될 때마다 Bean을 새로 생성하도록 해줘서, 실행 시점에 동적인 값을 주입받을 수 있게 해.
	기본적으로 Spring Bean은 Singleton이지만, @StepScope를 사용하면 매번 새로운 인스턴스를 생성해서 JobParameter 등 변화에 대응할 수 있어.
	즉, 실행마다 달라지는 데이터를 안전하게 사용하려면 필요하다.
	 */
	public InflearnApiReader inflearnApiReader(LectureClient lectureClient) {
		return new InflearnApiReader(lectureClient);
	}

	@Bean
	@StepScope
	public InflearnApiProcessor inflearnApiProcessor(SkillTagRepository skillTagRepository) {
		return new InflearnApiProcessor(skillTagRepository);
	}

	@Bean
	@StepScope
	public InflearnApiWriter inflearnApiWriter(LectureRepository lectureRepository,
		LectureSkillTagRepository lectureSkillTagRepository) {
		return new InflearnApiWriter(lectureRepository, lectureSkillTagRepository);
	}

}
