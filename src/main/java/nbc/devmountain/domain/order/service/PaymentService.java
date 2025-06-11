package nbc.devmountain.domain.order.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import nbc.devmountain.domain.order.client.TossApiClient;
import nbc.devmountain.domain.order.dto.ConfirmPaymentRequest;
import nbc.devmountain.domain.order.dto.OrderPaymentRequestDto;
import nbc.devmountain.domain.order.dto.PaymentResponseDto;
import nbc.devmountain.domain.order.dto.TossPaymentResponse;
import nbc.devmountain.domain.order.exception.OrderException;
import nbc.devmountain.domain.order.exception.OrderExceptionCode;
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
    private final TossApiClient tossApiClient;

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


    public TossPaymentResponse confirmPayment(ConfirmPaymentRequest request, Long userId) {
        Order order = orderRepository.findByOrderId(request.orderId())
                .orElseThrow(() -> new OrderException(OrderExceptionCode.ORDER_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new OrderException(OrderExceptionCode.USER_NOT_FOUND));

        // 토스 API 승인 요청
        TossPaymentResponse response = tossApiClient.confirmPayment(
                request.paymentKey(),
                request.orderId(),
                request.amount()
        );

        // 성공 여부 판단
        boolean isSuccess = response.isSuccessful();

        if (response.isSuccessful()) {
            user.updateMembershipLevel(User.MembershipLevel.PRO);
            userRepository.save(user);

            order.updateOrderStatus(OrderStatus.SUCCESS);
            orderRepository.save(order);
        }

        paymentRepository.save(Payment.builder()
                .order(order)
                .user(user)
                .paymentKey(response.paymentKey())
                .method(mapToMethodEnum(response.method())) // Toss는 "카드" 같은 문자열 줌
                .result(isSuccess ? Payment.Result.SUCCESS : Payment.Result.FAIL)
                .membershipLevelChangedTo(isSuccess ? Payment.MembershipLevel.PRO : null)
                .build());

        return response;
    }

    // 각 케이스별 ENUM 값 변환
    private Payment.Method mapToMethodEnum(String method) {
        return switch (method) {
            case "카드" -> Payment.Method.CARD;
            case "가상계좌" -> Payment.Method.TRANSFER;
            case "휴대폰" -> Payment.Method.PHONE;
            case "간편결제" -> Payment.Method.SIMPLE_PAYMENT;
            default -> throw new OrderException(OrderExceptionCode.ORDER_NOT_FOUND);
        };
    }
}
