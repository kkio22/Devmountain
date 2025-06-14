package nbc.devmountain.domain.recommendation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import nbc.devmountain.domain.recommendation.model.Recommendation;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
}