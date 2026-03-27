package com.pharmacy.orderservice.controller;

import com.pharmacy.orderservice.dto.CheckoutRequest;
import com.pharmacy.orderservice.dto.CheckoutSessionDto;
import com.pharmacy.orderservice.dto.OrderDto;
import com.pharmacy.orderservice.service.CheckoutService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    /** Step 1 — initiate checkout */
    @PostMapping("/start")
    public ResponseEntity<CheckoutSessionDto> startCheckout(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.status(201).body(checkoutService.startCheckout(userId));
    }

    /** Step 2 — set delivery address */
    @PostMapping("/{orderId}/address")
    public ResponseEntity<OrderDto> setAddress(
            @PathVariable Long orderId,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(checkoutService.setAddress(orderId, userId, request));
    }

    /** Step 3 — attach approved prescription */
    @PostMapping("/{orderId}/prescription-link")
    public ResponseEntity<OrderDto> linkPrescription(
            @PathVariable Long orderId,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @RequestParam Long prescriptionId) {
        return ResponseEntity.ok(checkoutService.linkPrescription(orderId, userId, prescriptionId));
    }

    /** Step 4 — confirm order */
    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<OrderDto> confirmOrder(
            @PathVariable Long orderId,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(checkoutService.confirmOrder(orderId, userId));
    }
}
