package nbc.devmountain.domain.lecture.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lecture_category_mapping")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LectureCategoryMapping {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long lectureCategoryMappingId;

	@ManyToOne
	@JoinColumn(name = "lecture_category_id")
	private LectureCategory lectureCategory;

	@ManyToOne
	@JoinColumn(name = "lecture_id")
	private Lecture lecture;

	@Builder
	public LectureCategoryMapping(LectureCategory lectureCategory, Lecture lecture){
		this.lectureCategory = lectureCategory;
		this.lecture = lecture;
	}
}
