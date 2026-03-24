package com.pharmacy.admin.controller;

import com.pharmacy.admin.dto.OrderResponse;
import com.pharmacy.admin.dto.OrderStatusUpdateRequest;
import com.pharmacy.admin.service.AdminOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(adminOrderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(adminOrderService.getOrderById(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable(name = "id") Long id,
            @RequestBody OrderStatusUpdateRequest request) {
        return ResponseEntity.ok(adminOrderService.updateOrderStatus(id, request.getStatus()));
    }
}