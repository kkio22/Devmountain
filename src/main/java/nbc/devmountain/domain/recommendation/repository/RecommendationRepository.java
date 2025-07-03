package nbc.devmountain.domain.recommendation.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import nbc.devmountain.domain.recommendation.model.Recommendation;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

	List<Recommendation> findAllByUserUserId(Long userUserId);


	Page<Recommendation> findByUserUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);


}