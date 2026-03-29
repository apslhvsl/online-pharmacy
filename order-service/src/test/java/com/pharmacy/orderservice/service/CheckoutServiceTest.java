package com.pharmacy.orderservice.service;

import com.pharmacy.orderservice.entity.Cart;
import com.pharmacy.orderservice.repository.*;
import com.pharmacy.orderservice.client.CatalogClient;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock CartService cartService;
    @Mock OrderRepository orderRepository;
    @Mock OrderStatusLogRepository statusLogRepository;
    @Mock AddressService addressService;
    @Mock CatalogClient catalogClient;
    @Mock OrderService orderService;
    @InjectMocks CheckoutService checkoutService;

    @Test
    void startCheckout_emptyCart_throwsIllegalState() {
        Cart emptyCart = Cart.builder().userId(1L).items(new ArrayList<>()).build();
        when(cartService.getCartEntity(1L)).thenReturn(emptyCart);

        assertThatThrownBy(() -> checkoutService.startCheckout(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cart is empty");
    }

    @Test
    void startCheckout_noCart_throwsEntityNotFound() {
        when(cartService.getCartEntity(1L))
                .thenThrow(new EntityNotFoundException("Cart is empty"));

        assertThatThrownBy(() -> checkoutService.startCheckout(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
