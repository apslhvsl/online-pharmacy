package com.pharmacy.orderservice.controller;


import com.pharmacy.orderservice.dto.CartDto;
import com.pharmacy.orderservice.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CartController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
        })
class CartControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean CartService cartService;

    @Test
    void getCart_returnsCart() throws Exception {
        var dto = CartDto.builder().userId(1L).items(Map.of(1L, 2)).build();
        when(cartService.getCart(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/orders/cart")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1));
    }
}