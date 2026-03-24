package com.pharmacy.orderservice.service;

import com.pharmacy.orderservice.dto.PaymentDto;
import com.pharmacy.orderservice.entity.Order;
import com.pharmacy.orderservice.entity.OrderStatus;
import com.pharmacy.orderservice.entity.Payment;
import com.pharmacy.orderservice.repository.OrderRepository;
import com.pharmacy.orderservice.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderStateMachine orderStateMachine;

    @Transactional
    public PaymentDto processPayment(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new EntityNotFoundException("Order not found: " + orderId);
        }

        // Simulate payment success
        Payment payment = Payment.builder()
                .orderId(orderId)
                .amount(order.getTotalAmount())
                .status("SUCCESS")
                .build();

        payment = paymentRepository.save(payment);

        // Transition order to CONFIRMED
        orderStateMachine.validate(order.getStatus(), OrderStatus.CONFIRMED);
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        return toDto(payment);
    }

    public PaymentDto getPaymentByOrder(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found for order: " + orderId));
        return toDto(payment);
    }

    private PaymentDto toDto(Payment payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}