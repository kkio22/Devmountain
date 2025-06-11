package nbc.devmountain.domain.order.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossPaymentResponse(
        String paymentKey,
        String orderId,
        String status,
        int totalAmount,
        String method,
        ZonedDateTime approvedAt,
        Card card
) {
    public boolean isSuccessful() {
        return "DONE".equalsIgnoreCase(status);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Card(
            String company,
            String number
    ) {}
}