package nbc.devmountain.domain.recommendation.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nbc.devmountain.domain.lecture.model.Lecture;
import nbc.devmountain.domain.lecture.model.WebSearch;
import nbc.devmountain.domain.lecture.model.Youtube;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationCount {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	private Lecture lecture;

	@ManyToOne
	private Youtube youtube;

	@ManyToOne
	private WebSearch webSearch;

	private Long count = 0L;

	public void increase() {
		this.count = (this.count == null ? 1L : this.count + 1);
	}

	@Builder
	public RecommendationCount(Lecture lecture, Youtube youtube, WebSearch webSearch, Long count) {
		this.lecture = lecture;
		this.youtube = youtube;
		this.webSearch = webSearch;
		this.count = count;
	}
}