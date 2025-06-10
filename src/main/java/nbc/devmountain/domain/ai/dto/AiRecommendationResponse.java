package nbc.devmountain.domain.ai.dto;

import java.util.List;

import lombok.Getter;

public class AiRecommendationResponse {
	private final List<Lecture> recommendations;

	public AiRecommendationResponse(List<Lecture> recommendations) {
		this.recommendations = recommendations;
	}

	@Getter
	public static class Lecture {
		private final String title;
		private final String url;
		private final String level;

		public Lecture(String title, String url, String level) {
			this.title = title;
			this.url = url;
			this.level = level;
		}
	}
}
