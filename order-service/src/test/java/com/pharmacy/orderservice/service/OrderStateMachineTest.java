package com.pharmacy.orderservice.service;

import com.pharmacy.orderservice.entity.OrderStatus;
import com.pharmacy.orderservice.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class OrderStateMachineTest {

    OrderStateMachine stateMachine = new OrderStateMachine();

    @Test
    void validTransition_paymentPendingToPaid() {
        assertThatNoException().isThrownBy(() ->
                stateMachine.validate(OrderStatus.PAYMENT_PENDING, OrderStatus.PAID));
    }

    @Test
    void validTransition_paidToPacked() {
        assertThatNoException().isThrownBy(() ->
                stateMachine.validate(OrderStatus.PAID, OrderStatus.PACKED));
    }

    @Test
    void validTransition_deliveredToReturnRequested() {
        assertThatNoException().isThrownBy(() ->
                stateMachine.validate(OrderStatus.DELIVERED, OrderStatus.RETURN_REQUESTED));
    }

    @Test
    void invalidTransition_deliveredToCustomerCancelled() {
        assertThatThrownBy(() ->
                stateMachine.validate(OrderStatus.DELIVERED, OrderStatus.CUSTOMER_CANCELLED))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void invalidTransition_paymentPendingToDelivered() {
        assertThatThrownBy(() ->
                stateMachine.validate(OrderStatus.PAYMENT_PENDING, OrderStatus.DELIVERED))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void invalidTransition_refundCompletedToAny() {
        assertThatThrownBy(() ->
                stateMachine.validate(OrderStatus.REFUND_COMPLETED, OrderStatus.PAID))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void validTransition_checkoutStartedToPaymentPending() {
        assertThatNoException().isThrownBy(() ->
                stateMachine.validate(OrderStatus.CHECKOUT_STARTED, OrderStatus.PAYMENT_PENDING));
    }
}
