package com.pharmacy.orderservice.controller;

import com.pharmacy.orderservice.dto.CartDto;
import com.pharmacy.orderservice.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

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
        CartDto dto = CartDto.builder()
                .userId(1L)
                .items(List.of())
                .subTotal(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .requiresPrescription(false)
                .build();
        when(cartService.getCart(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/orders/cart")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1));
    }
}
