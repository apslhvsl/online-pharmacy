package com.pharmacy.admin.service;

import com.pharmacy.admin.client.OrderClient;
import com.pharmacy.admin.dto.ApproveOrderRequest;
import com.pharmacy.admin.dto.OrderResponse;
import com.pharmacy.admin.dto.OrderStatusUpdateRequest;
import com.pharmacy.admin.dto.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final OrderClient orderClient;

    public PagedResponse<OrderResponse> getAllOrders(String status, Long userId, int page, int size, String sort) {
        return orderClient.getAllOrders(status, userId, page, size, sort);
    }

    public OrderResponse getOrderById(Long id) {
        return orderClient.getOrderById(id);
    }

    public OrderResponse updateOrderStatus(Long id, String status, String note, Long adminId) {
        return orderClient.updateOrderStatus(id, status, note, adminId);
    }

    public OrderResponse cancelOrder(Long id, String reason, Long adminId) {
        return orderClient.cancelOrder(id, reason, adminId);
    }

    public OrderResponse approveOrder(Long id, ApproveOrderRequest request, Long adminId) {
        return orderClient.approveOrder(id, request, adminId);
    }
}
