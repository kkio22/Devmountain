package nbc.devmountain.domain.lecture.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.common.response.ApiResponse;
import nbc.devmountain.domain.lecture.service.EmbeddingService;

@RestController
@RequiredArgsConstructor
@RequestMapping("lectures")
public class LectureController {

	private final Job lectureCrawlingJob;
	private final EmbeddingService embeddingService;
	private final JobLauncher jobLauncher;

	@GetMapping("inflearn")
	public ResponseEntity<ApiResponse<Void>> getLecture() {
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

	@PostMapping("embedding")
	public ResponseEntity<ApiResponse<Void>> embedLecture() {
		embeddingService.embedLecture();
		return ResponseEntity.ok(ApiResponse.success("임베딩에 성공했습니다", 200));
	}

}
