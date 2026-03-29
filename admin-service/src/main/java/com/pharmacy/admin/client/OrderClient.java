package com.pharmacy.admin.client;

import com.pharmacy.admin.dto.DashboardDto;
import com.pharmacy.admin.dto.OrderResponse;
import com.pharmacy.admin.dto.OrderStatusUpdateRequest;
import com.pharmacy.admin.dto.PagedResponse;
import com.pharmacy.admin.dto.SalesReportDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "order-service")
public interface OrderClient {

    @GetMapping("/api/orders/internal/all")
    PagedResponse<OrderResponse> getAllOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size);

    @GetMapping("/api/orders/internal/{id}")
    OrderResponse getOrderById(@PathVariable("id") Long id);

    @PatchMapping("/api/orders/internal/{id}/status")
    OrderResponse updateOrderStatus(@PathVariable("id") Long id,
                                    @RequestBody OrderStatusUpdateRequest request,
                                    @RequestHeader("X-User-Id") Long adminId);

    @PatchMapping("/api/orders/internal/{id}/cancel")
    OrderResponse cancelOrder(@PathVariable("id") Long id, @RequestBody OrderStatusUpdateRequest request,
                               @RequestHeader("X-User-Id") Long adminId);

    @GetMapping("/api/orders/internal/dashboard")
    DashboardDto getDashboard();

    @GetMapping("/api/orders/internal/reports/sales")
    SalesReportDto getSalesReport();
}
