package nbc.devmountain.domain.recommendation.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.common.response.ApiResponse;
import nbc.devmountain.common.util.security.CustomUserPrincipal;
import nbc.devmountain.domain.recommendation.model.Recommendation;
import nbc.devmountain.domain.recommendation.service.RecommendationService;

@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

	private final RecommendationService recommendationService;

	@GetMapping("/history")
	public ResponseEntity<ApiResponse<List<Recommendation>>> getRecommendationHistory(@AuthenticationPrincipal CustomUserPrincipal loginUser) {
		List<Recommendation> recommendationList = recommendationService.getRecommendationByUserId(loginUser.getUserId());
		return ResponseEntity.ok(
			ApiResponse.of(true,"강의 추천 기록 조회 ", HttpStatus.OK.value(), recommendationList));
	}
}
