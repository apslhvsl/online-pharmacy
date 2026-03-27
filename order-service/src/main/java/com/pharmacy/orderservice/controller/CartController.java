package com.pharmacy.orderservice.controller;

import com.pharmacy.orderservice.dto.AddItemRequest;
import com.pharmacy.orderservice.dto.CartDto;
import com.pharmacy.orderservice.dto.UpdateItemRequest;
import com.pharmacy.orderservice.service.CartService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartDto> getCart(@Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<CartDto> addItem(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody AddItemRequest request) {
        return ResponseEntity.ok(cartService.addItem(userId, request.getBatchId(), request.getQuantity()));
    }

    @PutMapping("/items/{batchId}")
    public ResponseEntity<CartDto> updateItem(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long batchId,
            @Valid @RequestBody UpdateItemRequest request) {
        return ResponseEntity.ok(cartService.updateItem(userId, batchId, request.getQuantity()));
    }

    @DeleteMapping("/items/{batchId}")
    public ResponseEntity<CartDto> removeItem(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long batchId) {
        return ResponseEntity.ok(cartService.removeItem(userId, batchId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
