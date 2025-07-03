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
import nbc.devmountain.domain.recommendation.repository.RecommendationCountRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

	private final RecommendationRepository recommendationRepository;
	private final RecommendationCountRepository recommendationCountRepository;

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
		Long count = 0L;
		switch (rec.getType()) {
			case VECTOR -> {
				if (rec.getLecture() != null) {
					count = recommendationCountRepository.findByLecture(rec.getLecture())
						.map(c -> c.getCount() != null ? c.getCount() : 0L)
						.orElse(0L);
				}
				return RecommendationHistory.fromLecture(rec, count);
			}
			case YOUTUBE -> {
				if (rec.getYoutube() != null) {
					count = recommendationCountRepository.findByYoutube(rec.getYoutube())
						.map(c -> c.getCount() != null ? c.getCount() : 0L)
						.orElse(0L);
				}
				return RecommendationHistory.fromYoutube(rec, count);
			}
			case BRAVE -> {
				if (rec.getWebSearch() != null) {
					count = recommendationCountRepository.findByWebSearch(rec.getWebSearch())
						.map(c -> c.getCount() != null ? c.getCount() : 0L)
						.orElse(0L);
				}
				return RecommendationHistory.fromWebSearch(rec, count);
			}
		}
		return null;
	}
}
