package nbc.devmountain.domain.order.controller;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.domain.order.dto.OrderPaymentRequestDto;
import nbc.devmountain.domain.order.dto.PaymentResponseDto;
import nbc.devmountain.domain.order.service.PaymentService;
import nbc.devmountain.domain.user.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/{orderId}/payment")
    public ResponseEntity<PaymentResponseDto> pay(@PathVariable Long orderId,
                                                  @AuthenticationPrincipal User user,
                                                  @RequestBody OrderPaymentRequestDto dto) {
        return ResponseEntity.ok(paymentService.processPayment(orderId, user.getUserId(), dto));
    }

    @GetMapping("/payments/{paymentId}")
    public ResponseEntity<PaymentResponseDto> get(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getPayment(paymentId));
    }
}
