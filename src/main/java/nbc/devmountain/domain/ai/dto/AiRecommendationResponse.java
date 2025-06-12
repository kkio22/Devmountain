package nbc.devmountain.domain.ai.dto;

import java.util.List;


public record AiRecommendationResponse(
	String message,
	List<Recommendation> recommendations
) {}

record Recommendation(
	String title,
	String url,
	String level,
	String thumbnailUrl
) {}
