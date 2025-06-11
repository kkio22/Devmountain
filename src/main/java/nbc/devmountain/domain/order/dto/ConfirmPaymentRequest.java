package nbc.devmountain.domain.order.dto;

public record ConfirmPaymentRequest(
        String paymentKey,
        String orderId,
        int amount,
        String cancelReason // 필요하다면 입력
) {}

