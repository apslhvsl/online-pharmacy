package com.pharmacy.orderservice.service;

import com.pharmacy.orderservice.dto.PaymentDto;
import com.pharmacy.orderservice.dto.PaymentInitiateRequest;
import com.pharmacy.orderservice.entity.*;
import com.pharmacy.orderservice.repository.OrderRepository;
import com.pharmacy.orderservice.repository.OrderStatusLogRepository;
import com.pharmacy.orderservice.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusLogRepository statusLogRepository;
    private final OrderStateMachine orderStateMachine;

    @Transactional
    public PaymentDto initiatePayment(PaymentInitiateRequest request, Long userId) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + request.getOrderId()));
        if (!order.getUserId().equals(userId)) throw new EntityNotFoundException("Order not found");
        if (order.getStatus() != OrderStatus.PAYMENT_PENDING)
            throw new IllegalStateException("Order is not in PAYMENT_PENDING state");

        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(request.getPaymentMethod())
                .amount(order.getTotalAmount())
                .status(PaymentStatus.PENDING)
                .gatewayTxnRef(UUID.randomUUID().toString())
                .build();

        // COD — mark paid immediately
        if (request.getPaymentMethod() == PaymentMethod.COD) {
            payment.setStatus(PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now());
            transitionOrder(order, OrderStatus.PAID, userId, "COD payment confirmed");
        }

        return toDto(paymentRepository.save(payment));
    }

    public PaymentDto getPaymentByOrder(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found for order: " + orderId));
    }

    private void transitionOrder(Order order, OrderStatus next, Long changedBy, String note) {
        orderStateMachine.validate(order.getStatus(), next);
        statusLogRepository.save(OrderStatusLog.builder()
                .order(order)
                .fromStatus(order.getStatus().name())
                .toStatus(next.name())
                .changedBy(changedBy)
                .note(note)
                .build());
        order.setStatus(next);
        orderRepository.save(order);
    }

    private PaymentDto toDto(Payment p) {
        return PaymentDto.builder()
                .id(p.getId()).orderId(p.getOrder().getId())
                .paymentMethod(p.getPaymentMethod()).status(p.getStatus())
                .amount(p.getAmount()).gatewayTxnRef(p.getGatewayTxnRef())
                .paidAt(p.getPaidAt()).refundedAt(p.getRefundedAt())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
