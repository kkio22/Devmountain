package nbc.devmountain.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

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
) {}
