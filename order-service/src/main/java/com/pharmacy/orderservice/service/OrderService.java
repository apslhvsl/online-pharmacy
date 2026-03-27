package com.pharmacy.orderservice.service;

import com.pharmacy.orderservice.dto.DashboardDto;
import com.pharmacy.orderservice.dto.OrderDto;
import com.pharmacy.orderservice.dto.OrderStatusUpdateRequest;
import com.pharmacy.orderservice.dto.SalesReportDto;
import com.pharmacy.orderservice.entity.*;
import com.pharmacy.orderservice.repository.OrderRepository;
import com.pharmacy.orderservice.repository.OrderStatusLogRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusLogRepository statusLogRepository;
    private final OrderStateMachine orderStateMachine;

    public Page<OrderDto> getOrdersByUser(Long userId, OrderStatus status, Pageable pageable) {
        if (status != null) {
            return orderRepository.findByUserIdAndStatus(userId, status, pageable).map(this::toDto);
        }
        return orderRepository.findByUserId(userId, pageable).map(this::toDto);
    }

    public OrderDto getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
        if (!order.getUserId().equals(userId)) throw new EntityNotFoundException("Order not found: " + orderId);
        return toDto(order);
    }

    public OrderDto getOrderByIdAdmin(Long orderId) {
        return toDto(orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId)));
    }

    @Transactional
    public OrderDto cancelOrder(Long orderId, Long userId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
        if (!order.getUserId().equals(userId)) throw new EntityNotFoundException("Order not found: " + orderId);

        OrderStatus next = OrderStatus.CUSTOMER_CANCELLED;
        orderStateMachine.validate(order.getStatus(), next);
        logTransition(order, next, userId, reason);
        order.setStatus(next);
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto updateStatus(Long orderId, OrderStatusUpdateRequest request, Long changedBy) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
        orderStateMachine.validate(order.getStatus(), request.getStatus());
        logTransition(order, request.getStatus(), changedBy, request.getNote());
        order.setStatus(request.getStatus());
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto requestReturn(Long orderId, Long userId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
        if (!order.getUserId().equals(userId)) throw new EntityNotFoundException("Order not found: " + orderId);
        if (order.getStatus() != OrderStatus.DELIVERED)
            throw new IllegalStateException("Returns only allowed for DELIVERED orders");
        if (order.getUpdatedAt() != null && order.getUpdatedAt().isBefore(LocalDateTime.now().minusDays(7)))
            throw new IllegalStateException("Return window of 7 days has passed");

        orderStateMachine.validate(order.getStatus(), OrderStatus.RETURN_REQUESTED);
        logTransition(order, OrderStatus.RETURN_REQUESTED, userId, reason);
        order.setStatus(OrderStatus.RETURN_REQUESTED);
        return toDto(orderRepository.save(order));
    }

    public Page<OrderDto> getAllOrders(OrderStatus status, Long userId,
                                       LocalDateTime dateFrom, LocalDateTime dateTo,
                                       Pageable pageable) {
        return orderRepository.findWithFilters(status, userId, dateFrom, dateTo, pageable).map(this::toDto);
    }

    public DashboardDto getDashboard() {
        long totalOrders = orderRepository.count();
        List<Order> recent = orderRepository.findWithFilters(null, null, null, null,
                org.springframework.data.domain.PageRequest.of(0, 5)).getContent();

        BigDecimal todayRevenue = orderRepository
                .findWithFilters(OrderStatus.PAID, null, LocalDateTime.now().toLocalDate().atStartOfDay(), null,
                        org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardDto.builder()
                .totalOrders(totalOrders)
                .todayRevenue(todayRevenue)
                .recentOrders(recent.stream().map(this::toDto).toList())
                .build();
    }

    public SalesReportDto getSalesReport() {
        List<Order> completed = orderRepository
                .findWithFilters(OrderStatus.DELIVERED, null, null, null,
                        org.springframework.data.domain.Pageable.unpaged())
                .getContent();

        BigDecimal totalRevenue = completed.stream()
                .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Long, SalesReportDto.MedicineSalesSummary> summaryMap = new HashMap<>();
        for (Order order : completed) {
            if (order.getItems() == null) continue;
            for (var item : order.getItems()) {
                summaryMap.merge(
                        item.getMedicineId(),
                        SalesReportDto.MedicineSalesSummary.builder()
                                .medicineId(item.getMedicineId())
                                .medicineName(item.getMedicineName())
                                .totalQuantitySold(item.getQuantity())
                                .totalRevenue(item.getLineTotal() != null ? item.getLineTotal() : BigDecimal.ZERO)
                                .build(),
                        (ex, n) -> SalesReportDto.MedicineSalesSummary.builder()
                                .medicineId(ex.getMedicineId())
                                .medicineName(ex.getMedicineName())
                                .totalQuantitySold(ex.getTotalQuantitySold() + n.getTotalQuantitySold())
                                .totalRevenue(ex.getTotalRevenue().add(n.getTotalRevenue()))
                                .build()
                );
            }
        }

        List<SalesReportDto.MedicineSalesSummary> top = new ArrayList<>(summaryMap.values());
        top.sort((a, b) -> Integer.compare(b.getTotalQuantitySold(), a.getTotalQuantitySold()));

        return SalesReportDto.builder()
                .totalOrdersCompleted(completed.size())
                .totalRevenue(totalRevenue)
                .topMedicines(top)
                .build();
    }

    private void logTransition(Order order, OrderStatus next, Long changedBy, String note) {
        statusLogRepository.save(OrderStatusLog.builder()
                .orderId(order.getId())
                .fromStatus(order.getStatus().name())
                .toStatus(next.name())
                .changedBy(changedBy)
                .note(note)
                .build());
    }

    public OrderDto toDto(Order order) {
        List<OrderDto.OrderItemDto> itemDtos = order.getItems() == null ? List.of() :
                order.getItems().stream().map(i -> OrderDto.OrderItemDto.builder()
                        .batchId(i.getBatchId())
                        .medicineId(i.getMedicineId())
                        .medicineName(i.getMedicineName())
                        .unitPrice(i.getUnitPrice())
                        .quantity(i.getQuantity())
                        .lineTotal(i.getLineTotal())
                        .build()).toList();

        return OrderDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .addressId(order.getAddressId())
                .status(order.getStatus())
                .prescriptionId(order.getPrescriptionId())
                .subtotal(order.getSubtotal())
                .taxAmount(order.getTaxAmount())
                .deliveryCharge(order.getDeliveryCharge())
                .totalAmount(order.getTotalAmount())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(itemDtos)
                .build();
    }
}
