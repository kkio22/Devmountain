package nbc.devmountain.domain.order.service;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import nbc.devmountain.domain.order.dto.OrderResponseDto;
import nbc.devmountain.domain.order.dto.OrderStatusUpdateDto;
import nbc.devmountain.domain.order.exception.OrderException;
import nbc.devmountain.domain.order.exception.OrderExceptionCode;
import nbc.devmountain.domain.order.model.Order;
import nbc.devmountain.domain.order.model.OrderStatus;
import nbc.devmountain.domain.order.repository.OrderRepository;
import nbc.devmountain.domain.user.model.User;
import nbc.devmountain.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private static final int FIXED_PRICE = 15000; // 위 값은 .env에 담아서 사용하든 어떤 특정 값에 저장하도록 수정하기

    public OrderResponseDto createOrder(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        String generatedOrderId = generateUniqueOrderId();

        Order order = Order.builder()
                .user(user)
                .orderId(generatedOrderId)
                .price(FIXED_PRICE)
                .status(OrderStatus.PENDING)
                .build();
        return OrderResponseDto.from(orderRepository.save(order));
    }

    public List<OrderResponseDto> getOrdersByUser(Long userId) {
        return orderRepository.findByUser_UserId(userId).stream()
                .map(OrderResponseDto::from)
                .toList();
    }

    public OrderResponseDto getOrderById(Long orderId, Long currentUserId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderExceptionCode.ORDER_NOT_FOUND));

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new OrderException(OrderExceptionCode.USER_NOT_FOUND));

        if (!order.getId().equals(currentUserId) && user.getRole() != User.Role.ADMIN) {
            throw new OrderException(OrderExceptionCode.NO_PERMISSION);
        }

        return OrderResponseDto.from(order);
    }

    public void updateOrderStatus(Long orderId, OrderStatusUpdateDto dto, Long currentUserId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderExceptionCode.ORDER_NOT_FOUND));

        if (!order.getId().equals(currentUserId)) {
            throw new OrderException(OrderExceptionCode.NO_PERMISSION);
        }

        order.updateOrderStatus(dto.status());
    }

    private String generateOrderId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new SecureRandom();

        for (int i = 0; i < 15; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        return "ORDER_" + sb;
    }

    public String generateUniqueOrderId() {
        String orderId;
        do {
            orderId = generateOrderId();
        } while (orderRepository.existsByOrderId(orderId));
        return orderId;
    }
}
