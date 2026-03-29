package com.pharmacy.admin.controller;

import com.pharmacy.admin.dto.OrderResponse;
import com.pharmacy.admin.dto.OrderStatusUpdateRequest;
import com.pharmacy.admin.dto.PagedResponse;
import com.pharmacy.admin.service.AdminOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @Operation(summary = "List all orders", description = "Returns a paginated list of all orders across all users, with optional filters for status and user ID")
    @GetMapping
    public ResponseEntity<PagedResponse<OrderResponse>> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminOrderService.getAllOrders(status, userId, page, size));
    }

    @Operation(summary = "Get order by ID", description = "Returns the full details of a specific order including items, payment, and status history")
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(adminOrderService.getOrderById(id));
    }

    @Operation(summary = "Update order status", description = "Transitions an order to a new status and records the admin who performed the update")
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody OrderStatusUpdateRequest request,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long adminId) {
        return ResponseEntity.ok(adminOrderService.updateOrderStatus(id, request, adminId));
    }

    @Operation(summary = "Cancel an order", description = "Cancels an order on behalf of an admin, with an optional cancellation note")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long id,
            @RequestBody OrderStatusUpdateRequest request,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long adminId) {
        return ResponseEntity.ok(adminOrderService.cancelOrder(id, request.getNote(), adminId));
    }
}
