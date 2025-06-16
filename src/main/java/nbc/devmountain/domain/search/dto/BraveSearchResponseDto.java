package nbc.devmountain.domain.search.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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
            String description,
            String url,
            @JsonProperty("thumbnail") ThumbnailWrapper thumbnailWrapper
    ) {
        public String thumbnail() {
            if (thumbnailWrapper == null) return null;
            return thumbnailWrapper.src != null ? thumbnailWrapper.src : thumbnailWrapper.original;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record ThumbnailWrapper(String src, String original) {}
    }
}