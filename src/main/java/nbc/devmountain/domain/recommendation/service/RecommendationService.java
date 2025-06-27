package nbc.devmountain.domain.recommendation.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.domain.recommendation.model.Recommendation;
import nbc.devmountain.domain.recommendation.repository.RecommendationRepository;

@Service
@RequiredArgsConstructor
public class RecommendationService {

	private final RecommendationRepository recommendationRepository;

	public List<Recommendation> getRecommendationByUserId(Long userId) {
		return recommendationRepository.findByUserUserId(userId);
	}
}
