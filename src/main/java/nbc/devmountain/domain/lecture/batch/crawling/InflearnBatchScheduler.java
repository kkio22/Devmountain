package nbc.devmountain.domain.lecture.batch.crawling;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class InflearnBatchScheduler {

	private final JobLauncher jobLauncher;
	private final BatchJobConfig batchJobConfig;
	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;

	@Scheduled(cron = "0 0 2 * * *")
	public void runJob() {
		try{
			JobParameters parameters = new JobParametersBuilder()
				.addLong("timestamp", System.currentTimeMillis())
				.toJobParameters();

			jobLauncher.run(batchJobConfig.lectureCrawlingJob(jobRepository, transactionManager), parameters);
		} catch (Exception e){
			log.error("강의 스케줄 실행 중 오류 발생: {}", e.getMessage());
		}
	}

}