package nbc.devmountain.common.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
@Slf4j
public class BatchJobConfig {
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
			.retry(BatchException.class)
			.skipLimit(10)
			.skip(BatchException.class)
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
