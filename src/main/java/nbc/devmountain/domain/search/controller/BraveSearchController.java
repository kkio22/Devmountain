package nbc.devmountain.domain.search.controller;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.common.response.ApiResponse;
import nbc.devmountain.domain.search.dto.BraveSearchResponseDto;
import nbc.devmountain.domain.search.sevice.BraveSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/brave")
@RequiredArgsConstructor
public class BraveSearchController {

    private final BraveSearchService braveSearchService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<BraveSearchResponseDto.Result>>> search(@RequestParam String q) {
        BraveSearchResponseDto response = braveSearchService.search(q);
        List<BraveSearchResponseDto.Result> results = Optional.ofNullable(response.web())
                .map(BraveSearchResponseDto.Web::results)
                .orElse(Collections.emptyList());
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}
