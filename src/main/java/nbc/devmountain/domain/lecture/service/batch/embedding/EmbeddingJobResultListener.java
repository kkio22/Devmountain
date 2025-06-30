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

		// 배치 Job 끝난 후 동작
		/*
		읽기에 몇개의 데이터가 들어왔고, -> step
		쓰기애 몇개의 데이터를 넣었는지 알고 싶음 -> step
		그리고 job이 시작 시간, 끝난 시간도 표시할 수 있으면 표시하고 싶음 -> job
		 */

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
					"Step 이름: %s\n Job 시작 시간: %s\n 인프런에서 가져온 강의 수: %d\n DB에 저장한 강의 수: %d\n Job 종료 시간: %s\n",
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
