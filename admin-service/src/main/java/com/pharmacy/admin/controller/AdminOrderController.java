package com.pharmacy.admin.controller;

import com.pharmacy.admin.dto.OrderResponse;
import com.pharmacy.admin.dto.OrderStatus;
import com.pharmacy.admin.dto.OrderStatusUpdateRequest;
import com.pharmacy.admin.dto.ApproveOrderRequest;
import com.pharmacy.admin.dto.PagedResponse;
import com.pharmacy.admin.service.AdminOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @Operation(summary = "List all orders", description = "Returns a paginated list of all orders across all users, with optional filters for status and user ID")
    @GetMapping
    public ResponseEntity<PagedResponse<OrderResponse>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        log.info("Admin list orders | status={} userId={} sort={}", status, userId, sort);
        return ResponseEntity.ok(adminOrderService.getAllOrders(status != null ? status.name() : null, userId, page, size, sort));
    }

    @Operation(summary = "Get order by ID", description = "Returns the full details of a specific order including items, payment, and status history")
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        log.info("Admin get order | orderId={}", id);
        return ResponseEntity.ok(adminOrderService.getOrderById(id));
    }

    @Operation(summary = "Update order status", description = "Transitions an order to a new status")
    @PostMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody OrderStatusUpdateRequest request,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long adminId) {
        log.info("Admin update order status | orderId={} newStatus={} adminId={}", id, request.getStatus(), adminId);
        return ResponseEntity.ok(adminOrderService.updateOrderStatus(id, request.getStatus().name(), request.getNote(), adminId));
    }

    @Operation(summary = "Approve order", description = "Approves a PENDING_APPROVAL order, applies batch overrides, deducts stock, and transitions to PACKED")
    @PostMapping("/{id}/approve")
    public ResponseEntity<OrderResponse> approveOrder(
            @PathVariable Long id,
            @RequestBody(required = false) ApproveOrderRequest request,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long adminId) {
        log.info("Admin approve order | orderId={} adminId={}", id, adminId);
        return ResponseEntity.ok(adminOrderService.approveOrder(id,
                request != null ? request : new ApproveOrderRequest(), adminId));
    }

    @Operation(summary = "Cancel an order", description = "Cancels an order on behalf of an admin, with an optional cancellation note")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long id,
            @RequestBody(required = false) OrderStatusUpdateRequest request,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long adminId) {
        log.info("Admin cancel order | orderId={} adminId={}", id, adminId);
        String note = request != null ? request.getNote() : null;
        return ResponseEntity.ok(adminOrderService.cancelOrder(id, note, adminId));
    }
}
