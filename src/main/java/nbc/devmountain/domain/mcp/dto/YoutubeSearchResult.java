package nbc.devmountain.domain.mcp.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;

@Builder
public record YoutubeSearchResult(
        String title,
        String description,
        String url,
        String thumbnail
) {
    public static YoutubeSearchResult from(JsonNode item) {
        JsonNode snippet = item.get("snippet");
        JsonNode thumbnails = snippet != null ? snippet.get("thumbnails") : null;
        JsonNode high = thumbnails != null ? thumbnails.get("high") : null;
        JsonNode idNode = item.get("id");

        String videoId = idNode != null && idNode.get("videoId") != null
                ? idNode.get("videoId").asText() : null;

        return YoutubeSearchResult.builder()
                .title(snippet != null && snippet.get("title") != null ? snippet.get("title").asText() : "")
                .description(snippet != null && snippet.get("description") != null ? snippet.get("description").asText() : "")
                .url(videoId != null ? "https://www.youtube.com/watch?v=" + videoId : "")
                .thumbnail(high != null && high.get("url") != null ? high.get("url").asText() : "")
                .build();
    }
}