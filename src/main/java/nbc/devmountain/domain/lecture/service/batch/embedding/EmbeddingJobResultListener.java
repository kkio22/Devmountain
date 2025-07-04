package nbc.devmountain.domain.lecture.service.batch.embedding;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EmbeddingJobResultListener implements JobExecutionListener {

	@Override
	public void afterJob(JobExecution jobExecution) {



		//포맷 정의
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		for (StepExecution stepExecution : jobExecution.getStepExecutions()) {

			String stepName = stepExecution.getStepName();

			if (stepName.equals("saveEmbeddingLectureStep")) {
				long reader = stepExecution.getReadCount();

				long writer = stepExecution.getWriteCount();

				LocalDateTime startTime = jobExecution.getStartTime();

				LocalDateTime endTime = jobExecution.getEndTime();

				String formatStartTime = startTime.format(dateTimeFormatter);
				String formatEndTime = endTime.format(dateTimeFormatter);

				String summary = String.format(
					"Step 이름: %s\n Job 시작 시간: %s\n DB에서 가져온 강의 수: %d\n 임베딩한 강의 수: %d\n Job 종료 시간: %s\n",
					stepName, formatStartTime, reader, writer, formatEndTime);

				try (PrintWriter printWriter = new PrintWriter("embedding-batch-result.txt")) {
					printWriter.println(summary);
				} catch (Exception e) {
					log.error("배치 결과 저장에 실패했습니다.");
				}
			}
		}
	}

	@Override
	public void beforeJob(JobExecution jobExecution) {
		// 배치 Job 시작 전 동작
	}
}
