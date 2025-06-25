package nbc.devmountain.domain.mcp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nbc.devmountain.common.config.YoutubeSearchProperties;
import nbc.devmountain.domain.mcp.dto.YoutubeSearchResult;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class YoutubeSearchService {

    private final YoutubeSearchProperties youtubeSearchProperties;

    public List<YoutubeSearchResult> search(String keyword) {
        log.info("MCP 서버 유튜브 요청 받음: " + keyword);
        List<YoutubeSearchResult> results = new ArrayList<>();

        try {
            ProcessBuilder pb = new ProcessBuilder("node", "/app/youtube-mcp-server/dist", keyword);
            pb.environment().put("YOUTUBE_API_KEY", youtubeSearchProperties.getKey());
            pb.environment().put("YOUTUBE_TRANSCRIPT_LANG", youtubeSearchProperties.getTranscriptLang());
            pb.redirectErrorStream(true);

            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            log.info("MCP 응답 원본: " + output.toString());

            process.waitFor();

            // JSON 파싱
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonArray = mapper.readTree(output.toString());

            for (JsonNode item : jsonArray) {
                results.add(YoutubeSearchResult.from(item));
            }

        } catch (Exception e) {
            log.error("YouTube 검색 실패: {}", e.getMessage(), e);
        }

        return results;
    }
}