package nbc.devmountain.domain.recommendation.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import nbc.devmountain.domain.lecture.model.Lecture;
import nbc.devmountain.domain.lecture.model.WebSearch;
import nbc.devmountain.domain.lecture.model.Youtube;
import nbc.devmountain.domain.recommendation.model.RecommendationCount;

public interface RecommendationCountRepository extends JpaRepository <RecommendationCount, Long>{

	Optional<RecommendationCount> findByLecture(Lecture lecture);
	Optional<RecommendationCount> findByYoutube(Youtube youtube);
	Optional<RecommendationCount> findByWebSearch(WebSearch webSearch);
}
