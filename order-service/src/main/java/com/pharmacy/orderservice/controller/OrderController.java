package com.pharmacy.orderservice.controller;

import com.pharmacy.orderservice.dto.*;
import com.pharmacy.orderservice.entity.OrderStatus;
import com.pharmacy.orderservice.service.CartService;
import com.pharmacy.orderservice.service.OrderService;
import com.pharmacy.orderservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final CartService cartService;

    // ── Customer endpoints ───────────────────────────────────────────

    @GetMapping
    public ResponseEntity<Page<OrderDto>> getMyOrders(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrdersByUser(userId, status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrder(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id, userId));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderDto> cancelOrder(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @RequestBody CancelRequest request) {
        return ResponseEntity.ok(orderService.cancelOrder(id, userId, request.getReason()));
    }

    @PostMapping("/{id}/reorder")
    public ResponseEntity<CartDto> reorder(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        OrderDto order = orderService.getOrderById(id, userId);
        // Add all items back to cart (skips out-of-stock silently via CartService)
        for (var item : order.getItems()) {
            try {
                cartService.addItem(userId, item.getBatchId(), item.getQuantity());
            } catch (Exception ignored) {
                // out-of-stock items skipped
            }
        }
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<OrderDto> requestReturn(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @RequestBody ReturnRequest request) {
        return ResponseEntity.ok(orderService.requestReturn(id, userId, request.getReason()));
    }

    // ── Payment endpoints ────────────────────────────────────────────

    @PostMapping("/payments/initiate")
    public ResponseEntity<PaymentDto> initiatePayment(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody PaymentInitiateRequest request) {
        return ResponseEntity.ok(paymentService.initiatePayment(request, userId));
    }

    @GetMapping("/payments/{orderId}")
    public ResponseEntity<PaymentDto> getPayment(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long orderId) {
        // validates ownership — getOrderById throws 404 if order doesn't belong to user
        orderService.getOrderById(orderId, userId);
        return ResponseEntity.ok(paymentService.getPaymentByOrder(orderId));
    }

    // ── Internal endpoints (Admin Service via Feign) ─────────────────

    @GetMapping("/internal/all")
    public ResponseEntity<Page<OrderDto>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) java.time.LocalDateTime dateFrom,
            @RequestParam(required = false) java.time.LocalDateTime dateTo,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(orderService.getAllOrders(status, userId, dateFrom, dateTo, pageable));
    }

    @GetMapping("/internal/{id}")
    public ResponseEntity<OrderDto> getOrderByIdInternal(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderByIdAdmin(id));
    }

    @PatchMapping("/internal/{id}/status")
    public ResponseEntity<OrderDto> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long adminId) {
        return ResponseEntity.ok(orderService.updateStatus(id, request, adminId));
    }

    @PatchMapping("/internal/{id}/cancel")
    public ResponseEntity<OrderDto> cancelOrderInternal(
            @PathVariable Long id,
            @RequestBody CancelRequest request,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long adminId) {
        OrderStatusUpdateRequest statusRequest = new OrderStatusUpdateRequest();
        statusRequest.setStatus(OrderStatus.ADMIN_CANCELLED);
        statusRequest.setNote(request.getReason());
        return ResponseEntity.ok(orderService.updateStatus(id, statusRequest, adminId));
    }

    @GetMapping("/internal/dashboard")
    public ResponseEntity<com.pharmacy.orderservice.dto.DashboardDto> getDashboard() {
        return ResponseEntity.ok(orderService.getDashboard());
    }

    @GetMapping("/internal/reports/sales")
    public ResponseEntity<com.pharmacy.orderservice.dto.SalesReportDto> getSalesReport() {
        return ResponseEntity.ok(orderService.getSalesReport());
    }
}
