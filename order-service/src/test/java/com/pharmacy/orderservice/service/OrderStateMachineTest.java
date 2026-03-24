package com.pharmacy.orderservice.service;

import com.pharmacy.orderservice.entity.OrderStatus;
import com.pharmacy.orderservice.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class OrderStateMachineTest {

    OrderStateMachine stateMachine = new OrderStateMachine();

    @Test
    void validTransition_pendingToConfirmed() {
        assertThatNoException().isThrownBy(() ->
                stateMachine.validate(OrderStatus.PENDING, OrderStatus.CONFIRMED));
    }

    @Test
    void invalidTransition_deliveredToCancelled() {
        assertThatThrownBy(() ->
                stateMachine.validate(OrderStatus.DELIVERED, OrderStatus.CANCELLED))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void invalidTransition_pendingToDelivered() {
        assertThatThrownBy(() ->
                stateMachine.validate(OrderStatus.PENDING, OrderStatus.DELIVERED))
                .isInstanceOf(InvalidStateTransitionException.class);
    }
}