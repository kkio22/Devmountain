package nbc.devmountain.domain.order.dto;

import nbc.devmountain.domain.order.model.Payment;

import java.time.LocalDateTime;

public record PaymentResponseDto(
        Long paymentId,
        String userEmail,
        String paymentKey,
        Payment.Method method,
        Payment.Result result,
        Payment.MembershipLevel membershipLevelChangedTo,
        LocalDateTime createdAt
) {
    public static PaymentResponseDto from(Payment payment) {
        return new PaymentResponseDto(
                payment.getPayId(),
                payment.getUser().getEmail(),
                payment.getPaymentKey(),
                payment.getMethod(),
                payment.getResult(),
                payment.getMembershipLevelChangedTo(),
                payment.getCreatedAt()
        );
    }
}
