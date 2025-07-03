package nbc.devmountain.domain.recommendation.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.domain.recommendation.dto.RecommendationHistory;
import nbc.devmountain.domain.recommendation.model.Recommendation;
import nbc.devmountain.domain.recommendation.repository.RecommendationRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

	private final RecommendationRepository recommendationRepository;

	@Transactional(readOnly = true)
	public List<RecommendationHistory> getRecommendationV1(Long userId) {
		List<Recommendation> recList = recommendationRepository.findAllByUserUserId(userId);
		return recList.stream().map(this::toDto).toList();
	}

	@Transactional(readOnly = true)
	public Page<RecommendationHistory> getRecommendationV2(Long userId, Pageable pageable) {
		Page<Recommendation> page = recommendationRepository.findByUserUserIdOrderByCreatedAtDesc(userId, pageable);

		return page.map(this::toDto);
	}

	private RecommendationHistory toDto(Recommendation rec) {
		return switch (rec.getType()) {
			case VECTOR -> RecommendationHistory.fromLecture(rec);
			case YOUTUBE -> RecommendationHistory.fromYoutube(rec);
			case BRAVE -> RecommendationHistory.fromWebSearch(rec);
		};
	}
}
