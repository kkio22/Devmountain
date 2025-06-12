package nbc.devmountain.domain.lecture.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import nbc.devmountain.domain.lecture.model.Lecture;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {

	void deleteByCrawledAtBefore(LocalDateTime today);

	List<Lecture> findByIsEmbeddedFalse();
}
