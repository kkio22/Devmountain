package nbc.devmountain.domain.recommendation.controller;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.common.response.ApiResponse;
import nbc.devmountain.common.util.security.CustomUserPrincipal;
import nbc.devmountain.domain.recommendation.dto.RecommendationHistoryPageDto;
import nbc.devmountain.domain.recommendation.service.RecommendationService;

@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

	private final RecommendationService recommendationService;

	@GetMapping("/history/paged")
	public ResponseEntity<ApiResponse<RecommendationHistoryPageDto>> getRecommendationHistoryPaged(
		@AuthenticationPrincipal CustomUserPrincipal loginUser,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		Pageable pageable = PageRequest.of(page, size);
		RecommendationHistoryPageDto recommendationPage = recommendationService.getRecommendationHistoryByUserIdPaged(loginUser.getUserId(), pageable);
		return ResponseEntity.ok(
			ApiResponse.of(true, "강의 추천 기록 페이지 조회", HttpStatus.OK.value(), recommendationPage));
	}
}
