package nbc.devmountain.domain.order.controller;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.domain.order.dto.OrderResponseDto;
import nbc.devmountain.domain.order.dto.OrderStatusUpdateDto;
import nbc.devmountain.domain.order.service.OrderService;
import nbc.devmountain.domain.user.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDto> create(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(orderService.createOrder(user.getUserId()));
    }

    @GetMapping("/me")
    public ResponseEntity<List<OrderResponseDto>> getMyOrders(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(orderService.getOrdersByUser(user.getUserId()));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @PatchMapping("/{orderId}")
    public ResponseEntity<Void> updateStatus(@PathVariable Long orderId,
                                             @RequestBody OrderStatusUpdateDto dto) {
        orderService.updateOrderStatus(orderId, dto);
        return ResponseEntity.ok().build();
    }
}