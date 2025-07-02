package nbc.devmountain.domain.lecture.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nbc.devmountain.common.response.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lectures")
@Getter
public class LectureController {

	@Value("${security.api.key}")
	private String apiKey;
	private final Job lectureCrawlingJob;
	private final JobLauncher jobLauncher;
	private final Job lectureEmbeddingJob;


	@GetMapping("/inflearn")
	public ResponseEntity<ApiResponse<Void>> getLecture(@RequestHeader("X-API-KEY") String requestKey) {
		if(!requestKey.equals(apiKey)){
			return ResponseEntity.ok(ApiResponse.error("잘못된 API KEY입니다.", 404));
		}
		try {
			JobParameters jobParameters = new JobParametersBuilder()
				.addLong("time", System.currentTimeMillis())
				.toJobParameters();

			jobLauncher.run(lectureCrawlingJob, jobParameters);
			return ResponseEntity.ok(ApiResponse.success("데이터를 저장에 성공했습니다.", 200));
		}catch (Exception e){
			return ResponseEntity.ok(ApiResponse.error("데이터 크롤링에 실패했습니다", 404));
		}
	}

	@PostMapping("/embedding")
	public ResponseEntity<ApiResponse<Void>> embedLecture(@RequestHeader("X-API-KEY") String requestKey) {
		if(!requestKey.equals(apiKey)){
			return ResponseEntity.ok(ApiResponse.error("잘못된 API KEY입니다.", 404));
		}
		try {
			JobParameters jobParameters = new JobParametersBuilder()
				.addLong("time", System.currentTimeMillis())
				.toJobParameters();

			jobLauncher.run(lectureEmbeddingJob, jobParameters);
			return ResponseEntity.ok(ApiResponse.success("데이터를 임베딩에 성공했습니다.", 200));
		}catch (Exception e){
			return ResponseEntity.ok(ApiResponse.error("데이터 임베딩에 실패했습니다", 404));
		}
	}


}
