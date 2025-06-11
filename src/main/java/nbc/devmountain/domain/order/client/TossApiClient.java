package nbc.devmountain.domain.order.client;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.common.config.TossPaymentProperties;
import nbc.devmountain.domain.order.dto.TossPaymentResponse;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TossApiClient {
    private final RestTemplate restTemplate;
    private final TossPaymentProperties properties;

    public TossPaymentResponse confirmPayment(String paymentKey, String orderId, int amount) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Base64 인코딩된 인증 키 생성
        String encodedAuth = Base64.getEncoder().encodeToString((properties.getSecretKey() + ":").getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedAuth);

        Map<String, Object> body = Map.of(
                "paymentKey", paymentKey,
                "orderId", orderId,
                "amount", amount
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<TossPaymentResponse> response = restTemplate.exchange(
                properties.getConfirmUrl(),
                HttpMethod.POST,
                request,
                TossPaymentResponse.class
        );

        return response.getBody();
    }
}
