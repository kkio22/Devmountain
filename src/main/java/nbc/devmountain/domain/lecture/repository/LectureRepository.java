package nbc.devmountain.domain.lecture.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import nbc.devmountain.domain.lecture.model.Lecture;
@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {
	@Query("SELECT l FROM Lecture l WHERE l.title LIKE %:keyword% OR l.description LIKE %:keyword%")
	List<Lecture> findRelevantLectures(@Param("keyword") String keyword);
}
