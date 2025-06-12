package nbc.devmountain.domain.lecture.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lecture_skill_tag",  uniqueConstraints = @UniqueConstraint(columnNames = {"lecture_id", "skill_tag_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LectureSkillTag {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long lectureSkillTagId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "skill_tag_id")
	private SkillTag skillTag;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "lecture_id")
	private Lecture lecture;

	@Builder
	public LectureSkillTag(SkillTag skillTag, Lecture lecture){
		this.skillTag = skillTag;
		this.lecture = lecture;
	}
}
