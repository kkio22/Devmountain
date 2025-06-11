package nbc.devmountain.domain.search.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BraveSearchResponseDto(
        Web web
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Web(
            List<Result> results
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(
            String title,
            String url,
            String description
    ) {}
}