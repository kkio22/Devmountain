package nbc.devmountain.domain.order.dto;

import nbc.devmountain.domain.order.model.OrderStatus;

public record OrderStatusUpdateDto(
        OrderStatus status
) {}
