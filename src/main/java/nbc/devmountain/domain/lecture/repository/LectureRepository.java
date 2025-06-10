package nbc.devmountain.domain.lecture.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import nbc.devmountain.domain.lecture.model.Lecture;

public interface LectureRepository extends JpaRepository<Lecture, Long> {

	@Query("""
		    SELECT l FROM Lecture l
		    JOIN FETCH l.category c
		    WHERE l.name LIKE %:keyword%
		       OR l.summary LIKE %:keyword%
		       OR l.instructor LIKE %:keyword%
		""")
	List<Lecture> findRelevantLectures(@Param("keyword") String keyword);
}
