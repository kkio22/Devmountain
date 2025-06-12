package nbc.devmountain.domain.order.controller;

import lombok.RequiredArgsConstructor;
import nbc.devmountain.common.response.ApiResponse;
import nbc.devmountain.common.util.security.CustomUserPrincipal;
import nbc.devmountain.domain.order.dto.OrderResponseDto;
import nbc.devmountain.domain.order.dto.OrderStatusUpdateDto;
import nbc.devmountain.domain.order.service.OrderService;
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
    public ResponseEntity<ApiResponse<OrderResponseDto>> create(@AuthenticationPrincipal CustomUserPrincipal user) {
        OrderResponseDto response = orderService.createOrder(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getMyOrders(@AuthenticationPrincipal CustomUserPrincipal user) {
        List<OrderResponseDto> response = orderService.getOrdersByUser(user.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponseDto>> getOrder(@PathVariable Long orderId,
                                                                  @AuthenticationPrincipal CustomUserPrincipal user) {
        OrderResponseDto response = orderService.getOrderById(orderId, user.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Void>> updateStatus(@PathVariable Long orderId,
                                                          @RequestBody OrderStatusUpdateDto dto,
                                                          @AuthenticationPrincipal CustomUserPrincipal user) {
        orderService.updateOrderStatus(orderId, dto, user.getUserId());
        return ResponseEntity.ok(ApiResponse.success("주문 상태가 성공적으로 수정되었습니다.", 200));
    }
}