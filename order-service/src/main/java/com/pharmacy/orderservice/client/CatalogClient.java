package com.pharmacy.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Map;

@FeignClient(name = "catalog-service")
public interface CatalogClient {

    @GetMapping("/api/catalog/medicines/{id}")
    Map<String, Object> getMedicineById(@PathVariable(name = "id") Long id);

    @GetMapping("/api/catalog/medicines/{id}/stock-check")
    Map<String, Object> checkStock(
            @PathVariable(name = "id") Long medicineId,
            @RequestParam(name = "quantity") Integer quantity
    );

    @GetMapping("/api/catalog/prescriptions/{id}/status")
    String getPrescriptionStatus(@PathVariable(name = "id") Long prescriptionId);

    @org.springframework.web.bind.annotation.PutMapping("/api/catalog/medicines/{id}/stock/deduct")
    void deductStock(
            @PathVariable(name = "id") Long medicineId,
            @RequestParam(name = "quantity") Integer quantity
    );
}