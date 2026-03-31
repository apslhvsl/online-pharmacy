package com.pharmacy.orderservice.service;

import com.pharmacy.orderservice.dto.OrderStatusUpdateRequest;
import com.pharmacy.orderservice.entity.*;
import com.pharmacy.orderservice.exception.InvalidStateTransitionException;
import com.pharmacy.orderservice.repository.OrderRepository;
import com.pharmacy.orderservice.repository.OrderStatusLogRepository;
import com.pharmacy.orderservice.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock OrderStatusLogRepository statusLogRepository;
    @Mock OrderStateMachine orderStateMachine;
    @Mock PaymentRepository paymentRepository;
    @Mock OrderEventPublisher orderEventPublisher;
    @InjectMocks OrderService orderService;

    private Order buildOrder(Long id, Long userId, OrderStatus status) {
        Order o = new Order();
        o.setId(id);
        o.setUserId(userId);
        o.setStatus(status);
        o.setOrderNumber("RX-2025-00001");
        o.setTotalAmount(new BigDecimal("500.00"));
        o.setCreatedAt(LocalDateTime.now());
        o.setUpdatedAt(LocalDateTime.now());
        return o;
    }

    // ── getOrderById ─────────────────────────────────────────────────

    @Test
    void getOrderById_ownedOrder_returnsDto() {
        Order order = buildOrder(1L, 10L, OrderStatus.PAID);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());

        var dto = orderService.getOrderById(1L, 10L);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void getOrderById_notFound_throwsEntityNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(99L, 10L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getOrderById_wrongUser_throwsEntityNotFound() {
        Order order = buildOrder(1L, 10L, OrderStatus.PAID);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.getOrderById(1L, 99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ── cancelOrder ──────────────────────────────────────────────────

    @Test
    void cancelOrder_validState_cancelsOrder() {
        Order order = buildOrder(1L, 10L, OrderStatus.PAID);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(statusLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());
        doNothing().when(orderStateMachine).validate(OrderStatus.PAID, OrderStatus.CUSTOMER_CANCELLED);

        var dto = orderService.cancelOrder(1L, 10L, "Changed my mind");

        assertThat(dto.getStatus()).isEqualTo(OrderStatus.CUSTOMER_CANCELLED);
        verify(orderEventPublisher).publishOrderUpdate(order);
    }

    @Test
    void cancelOrder_invalidTransition_throwsInvalidStateTransition() {
        Order order = buildOrder(1L, 10L, OrderStatus.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        doThrow(new InvalidStateTransitionException("Cannot transition"))
                .when(orderStateMachine).validate(OrderStatus.DELIVERED, OrderStatus.CUSTOMER_CANCELLED);

        assertThatThrownBy(() -> orderService.cancelOrder(1L, 10L, "reason"))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void cancelOrder_wrongUser_throwsEntityNotFound() {
        Order order = buildOrder(1L, 10L, OrderStatus.PAID);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(1L, 99L, "reason"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ── updateStatus ─────────────────────────────────────────────────

    @Test
    void updateStatus_validTransition_updatesOrder() {
        Order order = buildOrder(1L, 10L, OrderStatus.PAID);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(statusLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());
        doNothing().when(orderStateMachine).validate(OrderStatus.PAID, OrderStatus.PACKED);

        OrderStatusUpdateRequest req = new OrderStatusUpdateRequest();
        req.setStatus(OrderStatus.PACKED);
        req.setNote("Packed by warehouse");

        var dto = orderService.updateStatus(1L, req, 99L);

        assertThat(dto.getStatus()).isEqualTo(OrderStatus.PACKED);
        verify(orderEventPublisher).publishOrderUpdate(order);
    }

    // ── requestReturn ────────────────────────────────────────────────

    @Test
    void requestReturn_deliveredOrder_withinWindow_succeeds() {
        Order order = buildOrder(1L, 10L, OrderStatus.DELIVERED);
        order.setUpdatedAt(LocalDateTime.now().minusDays(2));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(statusLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());
        doNothing().when(orderStateMachine).validate(OrderStatus.DELIVERED, OrderStatus.RETURN_REQUESTED);

        var dto = orderService.requestReturn(1L, 10L, "Damaged item");

        assertThat(dto.getStatus()).isEqualTo(OrderStatus.RETURN_REQUESTED);
    }

    @Test
    void requestReturn_notDelivered_throwsIllegalState() {
        Order order = buildOrder(1L, 10L, OrderStatus.PAID);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.requestReturn(1L, 10L, "reason"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DELIVERED");
    }

    @Test
    void requestReturn_outsideReturnWindow_throwsIllegalState() {
        Order order = buildOrder(1L, 10L, OrderStatus.DELIVERED);
        order.setUpdatedAt(LocalDateTime.now().minusDays(10));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.requestReturn(1L, 10L, "reason"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("7 days");
    }

    // ── getOrdersByUser ──────────────────────────────────────────────

    @Test
    void getOrdersByUser_noStatusFilter_returnsAllOrders() {
        Order order = buildOrder(1L, 10L, OrderStatus.PAID);
        Page<Order> page = new PageImpl<>(List.of(order));
        when(orderRepository.findByUserId(eq(10L), any())).thenReturn(page);
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());

        var result = orderService.getOrdersByUser(10L, null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getOrdersByUser_withStatusFilter_returnsFilteredOrders() {
        Order order = buildOrder(1L, 10L, OrderStatus.PAID);
        Page<Order> page = new PageImpl<>(List.of(order));
        when(orderRepository.findByUserIdAndStatus(eq(10L), eq(OrderStatus.PAID), any())).thenReturn(page);
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());

        var result = orderService.getOrdersByUser(10L, OrderStatus.PAID, PageRequest.of(0, 10));

        assertThat(result.getContent().get(0).getStatus()).isEqualTo(OrderStatus.PAID);
    }
}
