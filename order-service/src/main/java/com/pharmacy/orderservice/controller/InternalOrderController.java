package com.pharmacy.orderservice.controller;

import com.pharmacy.orderservice.dto.*;
import com.pharmacy.orderservice.entity.OrderStatus;
import com.pharmacy.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Internal order endpoints — NOT gateway-routed.
 * Called by Admin Service via Feign only.
 * Gateway blocks /api/orders/internal/** from external access.
 */
@RestController
@RequestMapping("/api/orders/internal")
@RequiredArgsConstructor
public class InternalOrderController {

    private final OrderService orderService;

    @Operation(summary = "List all orders", description = "Returns a paginated list of all orders across all users with optional filters. For internal use by Admin Service only.")
    @GetMapping("/all")
    public ResponseEntity<Page<OrderDto>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) LocalDateTime dateFrom,
            @RequestParam(required = false) LocalDateTime dateTo,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(orderService.getAllOrders(status, userId, dateFrom, dateTo, pageable));
    }

    @Operation(summary = "Get order by ID", description = "Returns the full details of any order by ID without ownership checks. For internal use by Admin Service only.")
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderByIdAdmin(id));
    }

    @Operation(summary = "Update order status", description = "Transitions an order to a new status and records the admin who performed the update. For internal use by Admin Service only.")
    @PatchMapping("/{id}/status/{status}")
    public ResponseEntity<OrderDto> updateStatus(
            @PathVariable Long id,
            @PathVariable OrderStatus status,
            @RequestParam(required = false) String note,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long adminId) {
        OrderStatusUpdateRequest req = new OrderStatusUpdateRequest();
        req.setStatus(status);
        req.setNote(note);
        return ResponseEntity.ok(orderService.updateStatus(id, req, adminId));
    }

    @Operation(summary = "Cancel order", description = "Cancels an order on behalf of an admin with an optional note. For internal use by Admin Service only.")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderDto> cancelOrder(
            @PathVariable Long id,
            @RequestParam(required = false) String note,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long adminId) {
        OrderStatusUpdateRequest req = new OrderStatusUpdateRequest();
        req.setStatus(OrderStatus.ADMIN_CANCELLED);
        req.setNote(note);
        return ResponseEntity.ok(orderService.updateStatus(id, req, adminId));
    }

    @Operation(summary = "Get order dashboard", description = "Returns aggregated order statistics for the admin dashboard. For internal use by Admin Service only.")
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDto> getDashboard() {
        return ResponseEntity.ok(orderService.getDashboard());
    }

    @Operation(summary = "Get sales report", description = "Returns a sales report with revenue totals and order counts. For internal use by Admin Service only.")
    @GetMapping("/reports/sales")
    public ResponseEntity<SalesReportDto> getSalesReport() {
        return ResponseEntity.ok(orderService.getSalesReport());
    }
}
