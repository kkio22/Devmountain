package nbc.devmountain.domain.mcp.controller;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.domain.mcp.dto.YoutubeSearchResult;
import nbc.devmountain.domain.mcp.service.YoutubeSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import nbc.devmountain.domain.ai.service.MyAiService;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class YoutubeSearchController {

    private final YoutubeSearchService youtubeSearchService;

    private final MyAiService myAiService;

    @GetMapping("/api/youtube/chat")
    public ResponseEntity<String> searchWithChat(@RequestParam String query) {
        String aiResponse = myAiService.generateReply(query);
        return ResponseEntity.ok(aiResponse);
    }

    @GetMapping("/api/youtube/search")
    public List<YoutubeSearchResult> searchYoutube(@RequestParam String query) {
        return youtubeSearchService.search(query);
    }

}