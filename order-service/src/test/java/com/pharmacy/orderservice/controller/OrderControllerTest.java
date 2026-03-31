package com.pharmacy.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacy.orderservice.dto.*;
import com.pharmacy.orderservice.entity.OrderStatus;
import com.pharmacy.orderservice.entity.PaymentMethod;
import com.pharmacy.orderservice.service.CartService;
import com.pharmacy.orderservice.service.OrderService;
import com.pharmacy.orderservice.service.PaymentService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OrderController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
        })
class OrderControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean OrderService orderService;
    @MockBean PaymentService paymentService;
    @MockBean CartService cartService;

    private OrderDto sampleOrder() {
        return OrderDto.builder()
                .id(1L).orderNumber("RX-2025-00001").userId(10L)
                .status(OrderStatus.PAID).totalAmount(new BigDecimal("550.00"))
                .items(List.of()).build();
    }

    // ── GET /api/orders ──────────────────────────────────────────────

    @Test
    void getMyOrders_returnsPagedResponse() throws Exception {
        var page = new PageImpl<>(List.of(sampleOrder()));
        when(orderService.getOrdersByUser(eq(10L), isNull(), any())).thenReturn(page);

        mockMvc.perform(get("/api/orders").header("X-User-Id", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].orderNumber").value("RX-2025-00001"));
    }

    // ── GET /api/orders/{id} ─────────────────────────────────────────

    @Test
    void getOrder_found_returnsOrder() throws Exception {
        when(orderService.getOrderById(1L, 10L)).thenReturn(sampleOrder());

        mockMvc.perform(get("/api/orders/1").header("X-User-Id", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getOrder_notFound_returns404() throws Exception {
        when(orderService.getOrderById(99L, 10L))
                .thenThrow(new EntityNotFoundException("Order not found"));

        mockMvc.perform(get("/api/orders/99").header("X-User-Id", "10"))
                .andExpect(status().isNotFound());
    }

    // ── PATCH /api/orders/{id}/cancel ────────────────────────────────

    @Test
    void cancelOrder_validRequest_returnsUpdatedOrder() throws Exception {
        OrderDto cancelled = sampleOrder();
        cancelled.setStatus(OrderStatus.CUSTOMER_CANCELLED);
        when(orderService.cancelOrder(eq(1L), eq(10L), anyString())).thenReturn(cancelled);

        mockMvc.perform(patch("/api/orders/1/cancel")
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Changed my mind\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CUSTOMER_CANCELLED"));
    }

    // ── POST /api/orders/{id}/return ─────────────────────────────────

    @Test
    void requestReturn_validRequest_returnsUpdatedOrder() throws Exception {
        OrderDto returned = sampleOrder();
        returned.setStatus(OrderStatus.RETURN_REQUESTED);
        when(orderService.requestReturn(eq(1L), eq(10L), anyString())).thenReturn(returned);

        mockMvc.perform(post("/api/orders/1/return")
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Damaged item\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURN_REQUESTED"));
    }

    // ── POST /api/orders/payments/initiate/{orderId} ─────────────────

    @Test
    void initiatePayment_cod_returnsPaymentDto() throws Exception {
        PaymentDto dto = PaymentDto.builder()
                .id(100L).orderId(1L).paymentMethod(PaymentMethod.COD)
                .amount(new BigDecimal("550.00")).build();
        when(paymentService.initiatePayment(any(), eq(10L))).thenReturn(dto);

        mockMvc.perform(post("/api/orders/payments/initiate/1")
                        .header("X-User-Id", "10")
                        .param("paymentMethod", "COD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentMethod").value("COD"));
    }
}
