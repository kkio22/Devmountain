package nbc.devmountain.domain.lecture.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import nbc.devmountain.domain.lecture.model.Lecture;
import nbc.devmountain.domain.lecture.model.LectureSkillTag;
import nbc.devmountain.domain.lecture.model.SkillTag;

@Repository
public interface LectureSkillTagRepository extends JpaRepository<LectureSkillTag, Long> {
	boolean existsByLectureAndSkillTag(Lecture lecture, SkillTag skillTag);

	List<LectureSkillTag> findByLecture(Lecture lecture);
}
