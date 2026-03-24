package com.pharmacy.orderservice.controller;


import com.pharmacy.orderservice.dto.CartDto;
import com.pharmacy.orderservice.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartDto> getCart(
            @RequestHeader(name = "X-User-Id") Long userId) {
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<CartDto> addItem(
            @RequestHeader(name = "X-User-Id") Long userId,
            @RequestParam(name = "medicineId") Long medicineId,
            @RequestParam(name = "quantity") Integer quantity) {
        return ResponseEntity.ok(cartService.addItem(userId, medicineId, quantity));
    }

    @DeleteMapping("/items/{medicineId}")
    public ResponseEntity<CartDto> removeItem(
            @RequestHeader(name = "X-User-Id") Long userId,
            @PathVariable(name = "medicineId") Long medicineId) {
        return ResponseEntity.ok(cartService.removeItem(userId, medicineId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @RequestHeader(name = "X-User-Id") Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}