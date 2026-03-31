package com.pharmacy.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacy.orderservice.dto.AddressDto;
import com.pharmacy.orderservice.service.AddressService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AddressController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
        })
class AddressControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean AddressService addressService;

    private AddressDto sampleAddress() {
        return AddressDto.builder()
                .id(1L).userId(10L).label("Home")
                .line1("123 Main St").city("Mumbai").state("MH").pincode("400001")
                .isDefault(false).build();
    }

    // ── GET /api/orders/addresses ────────────────────────────────────

    @Test
    void getAddresses_returnsList() throws Exception {
        when(addressService.getAddresses(10L)).thenReturn(List.of(sampleAddress()));

        mockMvc.perform(get("/api/orders/addresses").header("X-User-Id", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].city").value("Mumbai"));
    }

    // ── POST /api/orders/addresses ───────────────────────────────────

    @Test
    void addAddress_validRequest_returns201() throws Exception {
        when(addressService.addAddress(eq(10L), any())).thenReturn(sampleAddress());

        String body = """
                {"line1":"123 Main St","city":"Mumbai","state":"MH","pincode":"400001"}
                """;

        mockMvc.perform(post("/api/orders/addresses")
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    // ── PUT /api/orders/addresses/{id} ───────────────────────────────

    @Test
    void updateAddress_validRequest_returnsUpdated() throws Exception {
        AddressDto updated = sampleAddress();
        updated.setCity("Pune");
        when(addressService.updateAddress(eq(1L), eq(10L), any())).thenReturn(updated);

        String body = """
                {"line1":"123 Main St","city":"Pune","state":"MH","pincode":"400001"}
                """;

        mockMvc.perform(put("/api/orders/addresses/1")
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Pune"));
    }

    @Test
    void updateAddress_notFound_returns404() throws Exception {
        when(addressService.updateAddress(eq(99L), eq(10L), any()))
                .thenThrow(new EntityNotFoundException("Address not found: 99"));

        mockMvc.perform(put("/api/orders/addresses/99")
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"line1\":\"x\",\"city\":\"x\",\"state\":\"x\",\"pincode\":\"x\"}"))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/orders/addresses/{id} ────────────────────────────

    @Test
    void deleteAddress_validRequest_returns204() throws Exception {
        doNothing().when(addressService).deleteAddress(1L, 10L);

        mockMvc.perform(delete("/api/orders/addresses/1").header("X-User-Id", "10"))
                .andExpect(status().isNoContent());
    }
}
