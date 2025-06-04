package nbc.devmountain.domain.order.dto;

import nbc.devmountain.domain.order.model.Payment;

public record OrderPaymentRequestDto(
        String paymentKey,
        Payment.Method method,
        Payment.MembershipLevel membershipLevelChangedTo
) {}
