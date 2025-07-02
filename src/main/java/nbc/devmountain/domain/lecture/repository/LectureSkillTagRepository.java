package nbc.devmountain.domain.lecture.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import nbc.devmountain.domain.lecture.model.Lecture;
import nbc.devmountain.domain.lecture.model.LectureSkillTag;
import nbc.devmountain.domain.lecture.model.SkillTag;

@Repository
public interface LectureSkillTagRepository extends JpaRepository<LectureSkillTag, Long> {

	@Query("SELECT lectureSkillTag FROM LectureSkillTag lectureSkillTag JOIN FETCH lectureSkillTag.skillTag WHERE lectureSkillTag.lecture = :lecture")
	List<LectureSkillTag> findByLectureWithSkillTag(@Param("lecture") Lecture lecture);
}
