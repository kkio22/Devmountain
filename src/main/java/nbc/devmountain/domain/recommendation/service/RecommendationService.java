package nbc.devmountain.domain.recommendation.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.domain.recommendation.dto.RecommendationDto;
import nbc.devmountain.domain.recommendation.repository.RecommendationRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

	private final RecommendationRepository recommendationRepository;

	public Page<RecommendationDto> getRecommendationByUserId(Long userId, Pageable pageable) {
		return recommendationRepository.findByUserUserIdOrderByCreatedAtDesc(userId, pageable)
			.map(RecommendationDto::from);
	}
}
