package nbc.devmountain.domain.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import nbc.devmountain.domain.recommendation.model.Recommendation;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RecommendationDto(
	Long lectureId,
	String thumbnailUrl,
	String title,
	String description,
	String instructor,
	String level,
	String url,
	String payPrice,
	String isFree,
	String type,
	Float score
) {
	public static RecommendationDto from(Recommendation recommendation) {
		return new RecommendationDto(
			recommendation.getLecture() != null ? recommendation.getLecture().getLectureId() : null,
			recommendation.getLecture() != null ? recommendation.getLecture().getThumbnailUrl() : null,
			recommendation.getLecture() != null ? recommendation.getLecture().getTitle() : null,
			recommendation.getLecture() != null ? recommendation.getLecture().getDescription() : null,
			recommendation.getLecture() != null ? recommendation.getLecture().getInstructor() : null,
			recommendation.getLecture() != null ? recommendation.getLecture().getLevelCode() : null,
			recommendation.getLecture() != null ? "https://www.inflearn.com/search?s=" + recommendation.getLecture().getTitle() : null,
			recommendation.getLecture() != null ? (recommendation.getLecture().isFree() ? "0" : recommendation.getLecture().getPayPrice().toPlainString()) : null,
			recommendation.getLecture() != null ? String.valueOf(recommendation.getLecture().isFree()) : null,
			recommendation.getType().toString(),
			recommendation.getScore()
		);
	}
}
