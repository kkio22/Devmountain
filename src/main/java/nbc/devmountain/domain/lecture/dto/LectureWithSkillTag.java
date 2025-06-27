package nbc.devmountain.domain.lecture.dto;

import java.util.List;

import nbc.devmountain.domain.lecture.model.Lecture;
import nbc.devmountain.domain.lecture.model.SkillTag;

public record LectureWithSkillTag(
	Lecture lecture,
	List<SkillTag> skillTags
) {
}
