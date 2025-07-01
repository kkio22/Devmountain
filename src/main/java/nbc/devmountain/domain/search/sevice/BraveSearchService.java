package nbc.devmountain.domain.search.sevice;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.common.config.BraveSearchProperties;
import nbc.devmountain.domain.search.dto.BraveSearchResponseDto;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Service
public class BraveSearchService {

    private final RestTemplate restTemplate;
    private final BraveSearchProperties braveSearchProperties;

    public BraveSearchResponseDto search(String query) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("X-Subscription-Token", braveSearchProperties.getNextKey());

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        String url = braveSearchProperties.getUrl() + "?q=" + query;

        ResponseEntity<BraveSearchResponseDto> response = restTemplate.exchange(
                url, HttpMethod.GET, requestEntity, BraveSearchResponseDto.class);

        return response.getBody();
    }
}