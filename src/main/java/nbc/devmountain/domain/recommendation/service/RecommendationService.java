package nbc.devmountain.domain.recommendation.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.domain.recommendation.dto.RecommendationHistoryDto;
import nbc.devmountain.domain.recommendation.dto.RecommendationHistoryPageDto;
import nbc.devmountain.domain.recommendation.model.Recommendation;
import nbc.devmountain.domain.recommendation.repository.RecommendationRepository;

@Service
@RequiredArgsConstructor
public class RecommendationService {

	private final RecommendationRepository recommendationRepository;

	@Transactional(readOnly = true)
	public RecommendationHistoryPageDto getRecommendationHistoryByUserIdPaged(Long userId, Pageable pageable) {
		Page<Recommendation> recommendationPage = recommendationRepository.findByUserUserIdOrderByCreatedAtDesc(userId, pageable);
		Page<RecommendationHistoryDto> dtoPage = recommendationPage.map(RecommendationHistoryDto::from);
		return RecommendationHistoryPageDto.from(dtoPage);
	}
}
