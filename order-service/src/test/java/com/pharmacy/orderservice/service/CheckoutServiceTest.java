package com.pharmacy.orderservice.service;


import com.pharmacy.orderservice.client.CatalogClient;
import com.pharmacy.orderservice.dto.CheckoutRequest;
import com.pharmacy.orderservice.entity.Cart;
import com.pharmacy.orderservice.exception.InsufficientStockException;
import com.pharmacy.orderservice.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock CartRepository cartRepository;
    @Mock OrderRepository orderRepository;
    @Mock IdempotencyKeyRepository idempotencyKeyRepository;
    @Mock CatalogClient catalogClient;
    @Mock OrderService orderService;
    @InjectMocks CheckoutService checkoutService;

    @Test
    void checkout_emptyCart_throwsException() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        var request = CheckoutRequest.builder().shippingAddress("123 Street").build();
        assertThatThrownBy(() -> checkoutService.checkout(1L, request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void checkout_insufficientStock_throwsException() {
        var cart = Cart.builder().userId(1L).items(Map.of(1L, 10)).build();
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        //when(idempotencyKeyRepository.findByKeyValue(null)).thenReturn(Optional.empty());
        when(catalogClient.checkStock(1L, 10))
                .thenReturn(Map.of("sufficient", false, "availableQuantity", 5));

        var request = CheckoutRequest.builder().shippingAddress("123 Street").build();
        assertThatThrownBy(() -> checkoutService.checkout(1L, request))
                .isInstanceOf(InsufficientStockException.class);
    }
}