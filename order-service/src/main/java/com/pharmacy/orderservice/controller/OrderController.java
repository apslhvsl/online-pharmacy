package com.pharmacy.orderservice.controller;

import com.pharmacy.orderservice.dto.*;
import com.pharmacy.orderservice.entity.OrderStatus;
import com.pharmacy.orderservice.entity.PaymentMethod;
import com.pharmacy.orderservice.service.CartService;
import com.pharmacy.orderservice.service.OrderService;
import com.pharmacy.orderservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @Operation(summary = "List my orders", description = "Returns a paginated list of orders placed by the authenticated user, with an optional filter by order status")
    @GetMapping
    public ResponseEntity<PagedResponse<OrderDto>> getMyOrders(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(PagedResponse.from(orderService.getOrdersByUser(userId, status, pageable)));
    }

    @Operation(summary = "Get order by ID", description = "Returns the full details of a specific order belonging to the authenticated user")
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrder(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id, userId));
    }

    @Operation(summary = "Cancel an order", description = "Cancels a confirmed order on behalf of the customer, with an optional cancellation reason")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderDto> cancelOrder(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @RequestBody CancelRequest request) {
        return ResponseEntity.ok(orderService.cancelOrder(id, userId, request.getReason()));
    }

    @Operation(summary = "Reorder a previous order", description = "Adds all items from a previous order back into the user's cart, silently skipping any out-of-stock items")
    @PostMapping("/{id}/reorder")
    public ResponseEntity<CartDto> reorder(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        OrderDto order = orderService.getOrderById(id, userId);
        for (var item : order.getItems()) {
            try {
                cartService.addItem(userId, item.getMedicineId(), item.getQuantity());
            } catch (Exception ignored) {
                // out-of-stock items skipped
            }
        }
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @Operation(summary = "Request a return", description = "Submits a return request for a delivered order with a reason provided by the customer")
    @PostMapping("/{id}/return")
    public ResponseEntity<OrderDto> requestReturn(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @RequestBody ReturnRequest request) {
        return ResponseEntity.ok(orderService.requestReturn(id, userId, request.getReason()));
    }

    // ── Payment endpoints ────────────────────────────────────────────

    @Operation(summary = "Initiate payment", description = "Creates a payment record for a confirmed order. Choose a payment method from the available options.")
    @PostMapping("/payments/initiate/{orderId}")
    public ResponseEntity<PaymentDto> initiatePayment(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long orderId,
            @RequestParam PaymentMethod paymentMethod) {
        PaymentInitiateRequest request = new PaymentInitiateRequest(orderId, paymentMethod);
        return ResponseEntity.ok(paymentService.initiatePayment(request, userId));
    }

    @Operation(summary = "Get payment for an order", description = "Returns the payment details associated with a specific order belonging to the authenticated user")
    @GetMapping("/payments/{orderId}")
    public ResponseEntity<PaymentDto> getPayment(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long orderId) {
        orderService.getOrderById(orderId, userId);
        return ResponseEntity.ok(paymentService.getPaymentByOrder(orderId));
    }
}
