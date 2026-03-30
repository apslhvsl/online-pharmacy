package com.pharmacy.orderservice.service;

import com.pharmacy.orderservice.entity.OrderStatus;
import com.pharmacy.orderservice.exception.InvalidStateTransitionException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class OrderStateMachine {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.ofEntries(
            Map.entry(OrderStatus.DRAFT,                   Set.of(OrderStatus.CHECKOUT_STARTED, OrderStatus.CUSTOMER_CANCELLED)),
            Map.entry(OrderStatus.CHECKOUT_STARTED,        Set.of(OrderStatus.PRESCRIPTION_PENDING, OrderStatus.PAYMENT_PENDING, OrderStatus.CUSTOMER_CANCELLED)),
            Map.entry(OrderStatus.PRESCRIPTION_PENDING,    Set.of(OrderStatus.PRESCRIPTION_APPROVED, OrderStatus.PRESCRIPTION_REJECTED)),
            Map.entry(OrderStatus.PRESCRIPTION_APPROVED,   Set.of(OrderStatus.PAYMENT_PENDING, OrderStatus.CUSTOMER_CANCELLED)),
            Map.entry(OrderStatus.PRESCRIPTION_REJECTED,   Set.of()),
            Map.entry(OrderStatus.PAYMENT_PENDING,         Set.of(OrderStatus.PAID, OrderStatus.PAYMENT_FAILED, OrderStatus.CUSTOMER_CANCELLED)),
            Map.entry(OrderStatus.PAID,                    Set.of(OrderStatus.PACKED, OrderStatus.ADMIN_CANCELLED, OrderStatus.CUSTOMER_CANCELLED)),
            Map.entry(OrderStatus.PACKED,                  Set.of(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.ADMIN_CANCELLED, OrderStatus.CUSTOMER_CANCELLED)),
            Map.entry(OrderStatus.OUT_FOR_DELIVERY,        Set.of(OrderStatus.DELIVERED)),
            Map.entry(OrderStatus.DELIVERED,               Set.of(OrderStatus.RETURN_REQUESTED)),
            Map.entry(OrderStatus.CUSTOMER_CANCELLED,      Set.of()),
            Map.entry(OrderStatus.ADMIN_CANCELLED,         Set.of(OrderStatus.REFUND_INITIATED)),
            Map.entry(OrderStatus.PAYMENT_FAILED,          Set.of()),
            Map.entry(OrderStatus.RETURN_REQUESTED,        Set.of(OrderStatus.REFUND_INITIATED)),
            Map.entry(OrderStatus.REFUND_INITIATED,        Set.of(OrderStatus.REFUND_COMPLETED)),
            Map.entry(OrderStatus.REFUND_COMPLETED,        Set.of())
    );

    public void validate(OrderStatus current, OrderStatus next) {
        Set<OrderStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(next)) {
            throw new InvalidStateTransitionException(
                    "Cannot transition from " + current + " to " + next);
        }
    }
}
