package com.pharmacy.orderservice.controller;

import com.pharmacy.orderservice.dto.CheckoutRequest;
import com.pharmacy.orderservice.dto.OrderDto;
import com.pharmacy.orderservice.service.CheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping
    public ResponseEntity<OrderDto> checkout(
            @RequestHeader(name = "X-User-Id") Long userId,
            @Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.status(201).body(checkoutService.checkout(userId, request));
    }
}