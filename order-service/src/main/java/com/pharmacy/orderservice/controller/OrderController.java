package com.pharmacy.orderservice.controller;


import com.pharmacy.orderservice.dto.OrderDto;
import com.pharmacy.orderservice.dto.PaymentDto;
import com.pharmacy.orderservice.entity.OrderStatus;
import com.pharmacy.orderservice.service.OrderService;
import com.pharmacy.orderservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<List<OrderDto>> getMyOrders(
            @RequestHeader(name = "X-User-Id") Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByUser(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrder(
            @RequestHeader(name = "X-User-Id") Long userId,
            @PathVariable(name = "id") Long orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId, userId));
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<OrderDto> cancelOrder(
            @RequestHeader(name = "X-User-Id") Long userId,
            @PathVariable(name = "id") Long orderId) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId, userId));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<PaymentDto> pay(
            @RequestHeader(name = "X-User-Id") Long userId,
            @PathVariable(name = "id") Long orderId) {
        return ResponseEntity.ok(paymentService.processPayment(orderId, userId));
    }

    @GetMapping("/{id}/payment")
    public ResponseEntity<PaymentDto> getPayment(
            @PathVariable(name = "id") Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrder(orderId));
    }

    // Internal — for admin service
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderDto> updateStatus(
            @PathVariable(name = "id") Long orderId,
            @RequestParam(name = "status") OrderStatus status) {
        return ResponseEntity.ok(orderService.updateStatus(orderId, status));
    }

    // Admin-only endpoint (no user filter)
    @GetMapping("/all")
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<OrderDto> getOrderAdmin(@PathVariable(name = "id") Long orderId) {
        return ResponseEntity.ok(orderService.getOrderByIdAdmin(orderId));
    }
}