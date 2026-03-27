package com.pharmacy.admin.service;

import com.pharmacy.admin.client.OrderClient;
import com.pharmacy.admin.dto.OrderResponse;
import com.pharmacy.admin.dto.OrderStatusUpdateRequest;
import com.pharmacy.admin.dto.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final OrderClient orderClient;

    public PagedResponse<OrderResponse> getAllOrders(String status, Long userId, int page, int size) {
        return orderClient.getAllOrders(status, userId, page, size);
    }

    public OrderResponse getOrderById(Long id) {
        return orderClient.getOrderById(id);
    }

    public OrderResponse updateOrderStatus(Long id, OrderStatusUpdateRequest request, Long adminId) {
        return orderClient.updateOrderStatus(id, request, adminId);
    }

    public void cancelOrder(Long id, String reason, Long adminId) {
        orderClient.cancelOrder(id, new OrderStatusUpdateRequest("ADMIN_CANCELLED", reason), adminId);
    }
}
