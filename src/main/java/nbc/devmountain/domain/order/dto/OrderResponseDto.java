package nbc.devmountain.domain.order.dto;

import nbc.devmountain.domain.order.model.Order;
import nbc.devmountain.domain.order.model.OrderStatus;

import java.time.LocalDateTime;

public record OrderResponseDto(
        Long orderId,
        String userEmail,
        Integer price,
        OrderStatus status,
        LocalDateTime createdAt
) {
    public static OrderResponseDto from(Order order) {
        return new OrderResponseDto(
                order.getOrderID(),
                order.getUser().getEmail(),
                order.getPrice(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}
