package com.pharmacy.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacy.orderservice.dto.CheckoutSessionDto;
import com.pharmacy.orderservice.dto.OrderDto;
import com.pharmacy.orderservice.entity.OrderStatus;
import com.pharmacy.orderservice.service.CheckoutService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CheckoutController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
        })
class CheckoutControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean CheckoutService checkoutService;

    // ── POST /api/orders/checkout/start ──────────────────────────────

    @Test
    void startCheckout_returnsSession() throws Exception {
        CheckoutSessionDto session = CheckoutSessionDto.builder()
                .orderId(1L).status(OrderStatus.CHECKOUT_STARTED).requiresPrescription(false).build();
        when(checkoutService.startCheckout(10L)).thenReturn(session);

        mockMvc.perform(post("/api/orders/checkout/start").header("X-User-Id", "10"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.status").value("CHECKOUT_STARTED"));
    }

    @Test
    void startCheckout_emptyCart_returns409() throws Exception {
        when(checkoutService.startCheckout(10L))
                .thenThrow(new IllegalStateException("Cart is empty"));

        mockMvc.perform(post("/api/orders/checkout/start").header("X-User-Id", "10"))
                .andExpect(status().isConflict());
    }

    // ── POST /api/orders/checkout/{orderId}/address ──────────────────

    @Test
    void setAddress_returnsOrderDto() throws Exception {
        OrderDto dto = OrderDto.builder().id(1L).status(OrderStatus.CHECKOUT_STARTED).build();
        when(checkoutService.setAddress(eq(1L), eq(10L), any())).thenReturn(dto);

        mockMvc.perform(post("/api/orders/checkout/1/address")
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"addressId\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // ── POST /api/orders/checkout/{orderId}/prescription-link ────────

    @Test
    void linkPrescription_returnsOrderDto() throws Exception {
        OrderDto dto = OrderDto.builder().id(1L).status(OrderStatus.PRESCRIPTION_APPROVED).build();
        when(checkoutService.linkPrescription(1L, 10L, 77L)).thenReturn(dto);

        mockMvc.perform(post("/api/orders/checkout/1/prescription-link")
                        .header("X-User-Id", "10")
                        .param("prescriptionId", "77"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PRESCRIPTION_APPROVED"));
    }

    // ── POST /api/orders/checkout/{orderId}/confirm ──────────────────

    @Test
    void confirmOrder_returnsPaymentPendingOrder() throws Exception {
        OrderDto dto = OrderDto.builder().id(1L).status(OrderStatus.PAYMENT_PENDING).build();
        when(checkoutService.confirmOrder(1L, 10L)).thenReturn(dto);

        mockMvc.perform(post("/api/orders/checkout/1/confirm").header("X-User-Id", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAYMENT_PENDING"));
    }
}
