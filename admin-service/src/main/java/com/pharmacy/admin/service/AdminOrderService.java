package com.pharmacy.admin.service;

import com.pharmacy.admin.client.OrderClient;
import com.pharmacy.admin.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final OrderClient orderClient;

    public List<OrderResponse> getAllOrders() {
        return orderClient.getAllOrders();
    }

    public OrderResponse getOrderById(Long id) {
        return orderClient.getOrderById(id);
    }

    public OrderResponse updateOrderStatus(Long id, String status) {
        return orderClient.updateOrderStatus(id, status);
    }
}