package nbc.devmountain.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RecommendationDto(
	String thumbnailUrl,
	String title,
	String description,
	String instructor,
	String level,
	String url
) {}
