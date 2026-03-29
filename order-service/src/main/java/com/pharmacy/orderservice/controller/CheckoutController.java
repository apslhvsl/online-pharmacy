package com.pharmacy.orderservice.controller;

import com.pharmacy.orderservice.dto.CheckoutRequest;
import com.pharmacy.orderservice.dto.CheckoutSessionDto;
import com.pharmacy.orderservice.dto.OrderDto;
import com.pharmacy.orderservice.service.CheckoutService;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "Start checkout", description = "Initiates a checkout session from the user's current cart, creating a pending order. This is step 1 of the checkout flow.")
    @PostMapping("/start")
    public ResponseEntity<CheckoutSessionDto> startCheckout(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.status(201).body(checkoutService.startCheckout(userId));
    }

    /** Step 2 — set delivery address */
    @Operation(summary = "Set delivery address", description = "Attaches a delivery address to the pending order. This is step 2 of the checkout flow.")
    @PostMapping("/{orderId}/address")
    public ResponseEntity<OrderDto> setAddress(
            @PathVariable Long orderId,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(checkoutService.setAddress(orderId, userId, request));
    }

    /** Step 3 — attach approved prescription */
    @Operation(summary = "Link a prescription to the order", description = "Associates an approved prescription with the pending order for medicines that require one. This is step 3 of the checkout flow.")
    @PostMapping("/{orderId}/prescription-link")
    public ResponseEntity<OrderDto> linkPrescription(
            @PathVariable Long orderId,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @RequestParam Long prescriptionId) {
        return ResponseEntity.ok(checkoutService.linkPrescription(orderId, userId, prescriptionId));
    }

    /** Step 4 — confirm order */
    @Operation(summary = "Confirm the order", description = "Finalises the order, deducts stock, and transitions the order to confirmed status. This is step 4 of the checkout flow.")
    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<OrderDto> confirmOrder(
            @PathVariable Long orderId,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(checkoutService.confirmOrder(orderId, userId));
    }
}
