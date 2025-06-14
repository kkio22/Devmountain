package nbc.devmountain.domain.ai.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AiRecommendationResponse(
	List<RecommendationDto> recommendations
) {}

