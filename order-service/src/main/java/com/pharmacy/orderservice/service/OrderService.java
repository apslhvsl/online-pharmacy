package com.pharmacy.orderservice.service;


import com.pharmacy.orderservice.dto.OrderDto;
import com.pharmacy.orderservice.entity.Order;
import com.pharmacy.orderservice.entity.OrderStatus;
import com.pharmacy.orderservice.repository.OrderRepository;
import com.pharmacy.orderservice.service.OrderStateMachine;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderStateMachine orderStateMachine;

    public List<OrderDto> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::toDto)
                .toList();
    }

    public OrderDto getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
        if (!order.getUserId().equals(userId)) {
            throw new EntityNotFoundException("Order not found: " + orderId);
        }
        return toDto(order);
    }

    public OrderDto getOrderByIdAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
        return toDto(order);
    }

    public OrderDto updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
        orderStateMachine.validate(order.getStatus(), newStatus);
        order.setStatus(newStatus);
        return toDto(orderRepository.save(order));
    }

    public OrderDto cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
        if (!order.getUserId().equals(userId)) {
            throw new EntityNotFoundException("Order not found: " + orderId);
        }
        orderStateMachine.validate(order.getStatus(), OrderStatus.CANCELLED);
        order.setStatus(OrderStatus.CANCELLED);
        return toDto(orderRepository.save(order));
    }

    public OrderDto toDto(Order order) {
        List<OrderDto.OrderItemDto> itemDtos = order.getItems() == null ? List.of() :
                order.getItems().stream().map(item -> OrderDto.OrderItemDto.builder()
                        .medicineId(item.getMedicineId())
                        .medicineName(item.getMedicineName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build()).toList();

        return OrderDto.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .prescriptionId(order.getPrescriptionId())
                .shippingAddress(order.getShippingAddress())
                .createdAt(order.getCreatedAt())
                .items(itemDtos)
                .build();
    }
    //remove this later if not works

    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }
}