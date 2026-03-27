package com.pharmacy.admin.client;

import com.pharmacy.admin.dto.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "order-service")
public interface OrderClient {

    @GetMapping("/api/orders/all")
    List<OrderResponse> getAllOrders();

    @PatchMapping("/api/orders/{id}/status")
    OrderResponse updateOrderStatus(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "status") String status);

    @GetMapping("/api/orders/admin/{id}")
    OrderResponse getOrderById(@PathVariable(name = "id") Long id);
}