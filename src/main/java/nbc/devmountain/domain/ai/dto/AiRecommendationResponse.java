package nbc.devmountain.domain.ai.dto;

import java.util.List;


public record AiRecommendationResponse(
	List<Recommendation> recommendations
) {}

record Recommendation(
	String title,
	String url,
	String level
) {}
