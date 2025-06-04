package nbc.devmountain.domain.order.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import nbc.devmountain.domain.order.dto.OrderPaymentRequestDto;
import nbc.devmountain.domain.order.dto.PaymentResponseDto;
import nbc.devmountain.domain.order.model.Order;
import nbc.devmountain.domain.order.model.OrderStatus;
import nbc.devmountain.domain.order.model.Payment;
import nbc.devmountain.domain.order.repository.OrderRepository;
import nbc.devmountain.domain.order.repository.PaymentRepository;
import nbc.devmountain.domain.user.model.User;
import nbc.devmountain.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public PaymentResponseDto processPayment(Long orderId, Long userId, OrderPaymentRequestDto dto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // todo API 연결
        Payment payment = Payment.builder()
                .order(order)
                .user(user)
                .paymentKey(dto.paymentKey())
                .method(dto.method())
                .result(Payment.Result.SUCCESS)
                .membershipLevelChangedTo(dto.membershipLevelChangedTo())
                .build();

        order.updateOrderStatus(OrderStatus.SUCCESS);
        return PaymentResponseDto.from(paymentRepository.save(payment));
    }

    public PaymentResponseDto getPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .map(PaymentResponseDto::from)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found"));
    }
}
