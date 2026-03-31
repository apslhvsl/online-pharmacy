package com.pharmacy.orderservice.service;

import com.pharmacy.orderservice.dto.PaymentInitiateRequest;
import com.pharmacy.orderservice.entity.*;
import com.pharmacy.orderservice.repository.OrderRepository;
import com.pharmacy.orderservice.repository.OrderStatusLogRepository;
import com.pharmacy.orderservice.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock PaymentRepository paymentRepository;
    @Mock OrderRepository orderRepository;
    @Mock OrderStatusLogRepository statusLogRepository;
    @Mock OrderStateMachine orderStateMachine;
    @Mock OrderEventPublisher orderEventPublisher;
    @InjectMocks PaymentService paymentService;

    private Order paymentPendingOrder(Long userId) {
        Order o = new Order();
        o.setId(1L);
        o.setUserId(userId);
        o.setStatus(OrderStatus.PAYMENT_PENDING);
        o.setOrderNumber("RX-2025-00001");
        o.setTotalAmount(new BigDecimal("550.00"));
        o.setCreatedAt(LocalDateTime.now());
        return o;
    }

    private Payment savedPayment(Order order, PaymentMethod method, PaymentStatus status) {
        Payment p = new Payment();
        p.setId(100L);
        p.setOrder(order);
        p.setPaymentMethod(method);
        p.setStatus(status);
        p.setAmount(order.getTotalAmount());
        p.setGatewayTxnRef("txn-ref-123");
        p.setCreatedAt(LocalDateTime.now());
        return p;
    }

    // ── initiatePayment ──────────────────────────────────────────────

    @Test
    void initiatePayment_cod_marksAsPaidImmediately() {
        Order order = paymentPendingOrder(10L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        doNothing().when(orderStateMachine).validate(OrderStatus.PAYMENT_PENDING, OrderStatus.PAID);
        when(statusLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(paymentRepository.save(any())).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(100L);
            return p;
        });

        PaymentInitiateRequest req = new PaymentInitiateRequest(1L, PaymentMethod.COD);
        var dto = paymentService.initiatePayment(req, 10L);

        assertThat(dto.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(dto.getPaidAt()).isNotNull();
        verify(orderEventPublisher).publishOrderUpdate(order);
    }

    @Test
    void initiatePayment_orderNotFound_throwsEntityNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.initiatePayment(new PaymentInitiateRequest(99L, PaymentMethod.COD), 10L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void initiatePayment_wrongUser_throwsEntityNotFound() {
        Order order = paymentPendingOrder(10L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> paymentService.initiatePayment(new PaymentInitiateRequest(1L, PaymentMethod.COD), 99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void initiatePayment_wrongStatus_throwsIllegalState() {
        Order order = paymentPendingOrder(10L);
        order.setStatus(OrderStatus.PAID);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> paymentService.initiatePayment(new PaymentInitiateRequest(1L, PaymentMethod.COD), 10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PAYMENT_PENDING");
    }

    // ── getPaymentByOrder ────────────────────────────────────────────

    @Test
    void getPaymentByOrder_found_returnsDto() {
        Order order = paymentPendingOrder(10L);
        Payment payment = savedPayment(order, PaymentMethod.COD, PaymentStatus.PAID);
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.of(payment));

        var dto = paymentService.getPaymentByOrder(1L);

        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getStatus()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    void getPaymentByOrder_notFound_throwsEntityNotFound() {
        when(paymentRepository.findByOrderId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPaymentByOrder(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
