package com.pharmacy.orderservice.service;

import com.pharmacy.orderservice.entity.OrderStatus;
import com.pharmacy.orderservice.exception.InvalidStateTransitionException;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class OrderStateMachine {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.PENDING,    Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED,  Set.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED),
            OrderStatus.PROCESSING, Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED),
            OrderStatus.SHIPPED,    Set.of(OrderStatus.DELIVERED),
            OrderStatus.DELIVERED,  Set.of(),
            OrderStatus.CANCELLED,  Set.of()
    );

    public void validate(OrderStatus current, OrderStatus next) {
        Set<OrderStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(next)) {
            throw new InvalidStateTransitionException(
                    "Cannot transition from " + current + " to " + next
            );
        }
    }
}