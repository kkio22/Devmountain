package nbc.devmountain.domain.recommendation.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import nbc.devmountain.domain.recommendation.model.Recommendation;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RecommendationHistory(
	Long chatRoomId,
	Long userId,
	LocalDateTime createdAt,
	Long lectureId,
	String title,
	String description,
	String instructor,
	String thumbnailUrl,
	String url,
	String lectureType,
	Float score,
	Long recommendCount
) {
	public static RecommendationHistory fromLecture(Recommendation r, Long count) {
		return new RecommendationHistory(
			r.getChatMessage().getChatRoom().getChatroomId(),
			r.getUser().getUserId(),
			r.getCreatedAt(),
			r.getLecture().getLectureId(),
			r.getLecture().getTitle(),
			r.getLecture().getDescription(),
			r.getLecture().getInstructor(),
			r.getLecture().getThumbnailUrl(),
			"https://www.inflearn.com/search?s=" +r.getLecture().getTitle(),
			r.getType().name(),
			r.getScore(),
			count
		);
	}

	public static RecommendationHistory fromYoutube(Recommendation r, Long count) {
		return new RecommendationHistory(
			r.getChatMessage().getChatRoom().getChatroomId(),
			r.getUser().getUserId(),
			r.getCreatedAt(),
			r.getYoutube().getId(),
			r.getYoutube().getTitle(),
			r.getYoutube().getDescription(),
			null,
			r.getYoutube().getThumbnailUrl(),
			r.getYoutube().getUrl(),
			r.getType().name(),
			r.getScore(),
			count
		);
	}

	public static RecommendationHistory fromWebSearch(Recommendation r, Long count) {
		return new RecommendationHistory(
			r.getChatMessage().getChatRoom().getChatroomId(),
			r.getUser().getUserId(),
			r.getCreatedAt(),
			r.getWebSearch().getId(),
			r.getWebSearch().getTitle(),
			r.getWebSearch().getDescription(),
			null,
			r.getWebSearch().getThumbnailUrl(),
			r.getWebSearch().getUrl(),
			r.getType().name(),
			r.getScore(),
			count
		);
	}
}