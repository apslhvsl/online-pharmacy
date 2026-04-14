package com.pharmacy.orderservice.controller;

import com.pharmacy.orderservice.dto.AddItemRequest;
import com.pharmacy.orderservice.dto.CartDto;
import com.pharmacy.orderservice.dto.UpdateItemRequest;
import com.pharmacy.orderservice.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/orders/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Get current cart", description = "Returns the active shopping cart for the authenticated user, including all items and totals")
    @GetMapping
    public ResponseEntity<CartDto> getCart(@Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        log.info("Get cart | userId={}", userId);
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @Operation(summary = "Add item to cart", description = "Adds a medicine to the authenticated user's cart by medicineId. The best available batch is selected automatically.")
    @PostMapping("/items")
    public ResponseEntity<CartDto> addItem(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody AddItemRequest request) {
        log.info("Add to cart | userId={} medicineId={} qty={}", userId, request.getMedicineId(), request.getQuantity());
        return ResponseEntity.ok(cartService.addItem(userId, request.getMedicineId(), request.getQuantity()));
    }

    @Operation(summary = "Update cart item quantity", description = "Sets a new quantity for an existing item in the cart identified by its batch ID")
    @PutMapping("/items/{batchId}")
    public ResponseEntity<CartDto> updateItem(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long batchId,
            @Valid @RequestBody UpdateItemRequest request) {
        log.info("Update cart item | userId={} batchId={} qty={}", userId, batchId, request.getQuantity());
        return ResponseEntity.ok(cartService.updateItem(userId, batchId, request.getQuantity()));
    }

    @Operation(summary = "Remove item from cart", description = "Removes a specific item from the cart identified by its batch ID")
    @DeleteMapping("/items/{batchId}")
    public ResponseEntity<CartDto> removeItem(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long batchId) {
        log.info("Remove cart item | userId={} batchId={}", userId, batchId);
        return ResponseEntity.ok(cartService.removeItem(userId, batchId));
    }

    @Operation(summary = "Clear the cart", description = "Removes all items from the authenticated user's cart")
    @DeleteMapping
    public ResponseEntity<Void> clearCart(@Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId) {
        log.info("Clear cart | userId={}", userId);
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
