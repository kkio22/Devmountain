package nbc.devmountain.domain.recommendation.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.common.response.ApiResponse;
import nbc.devmountain.common.util.security.CustomUserPrincipal;
import nbc.devmountain.domain.recommendation.dto.RecommendationHistory;
import nbc.devmountain.domain.recommendation.service.RecommendationService;

@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

	private final RecommendationService recommendationService;

	@GetMapping("/v1/history")
	public ResponseEntity<ApiResponse<List<RecommendationHistory>>> getRecommendationHistoryV1(
		@AuthenticationPrincipal CustomUserPrincipal loginUser) {
		List<RecommendationHistory> recommendations = recommendationService.getRecommendationV1(loginUser.getUserId());
		return ResponseEntity.ok(ApiResponse.of(true, "강의 추천 기록 조회 v1", HttpStatus.OK.value(), recommendations));
	}

	@GetMapping("/v2/history")
	public ResponseEntity<ApiResponse<List<RecommendationHistory>>> getRecommendationHistoryV2(
		@AuthenticationPrincipal CustomUserPrincipal loginUser, @PageableDefault(size = 10) Pageable pageable) {
		Page<RecommendationHistory> page = recommendationService.getRecommendationV2(loginUser.getUserId(),
			pageable);
		return ResponseEntity.ok(ApiResponse.of(true, "강의 추천 기록 조회 v2", HttpStatus.OK.value(), page.getContent()));
	}
}
