package com.pharmacy.admin.client;

import com.pharmacy.admin.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "order-service")
public interface OrderClient {

    @GetMapping("/api/orders/internal/all")
    PagedResponse<OrderResponse> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort);

    @GetMapping("/api/orders/internal/{id}")
    OrderResponse getOrderById(@PathVariable("id") Long id);

    @PostMapping("/api/orders/internal/{id}/status/{status}")
    OrderResponse updateOrderStatus(@PathVariable("id") Long id,
                                    @PathVariable("status") String status,
                                    @RequestParam(required = false) String note,
                                    @RequestHeader("X-User-Id") Long adminId);

    @PostMapping("/api/orders/internal/{id}/cancel")
    OrderResponse cancelOrder(@PathVariable("id") Long id,
                              @RequestParam(required = false) String note,
                              @RequestHeader("X-User-Id") Long adminId);

    @PostMapping("/api/orders/internal/{id}/approve")
    OrderResponse approveOrder(@PathVariable("id") Long id,
                               @RequestBody ApproveOrderRequest request,
                               @RequestHeader("X-User-Id") Long adminId);

    @GetMapping("/api/orders/internal/dashboard")
    DashboardDto getDashboard();

    @GetMapping("/api/orders/internal/reports/sales")
    SalesReportDto getSalesReport();
}
