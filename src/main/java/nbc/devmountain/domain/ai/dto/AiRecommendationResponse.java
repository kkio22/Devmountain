package nbc.devmountain.domain.ai.dto;

import java.util.List;


public record AiRecommendationResponse(
	String message,
	List<RecommendationDto> recommendations
) {}

