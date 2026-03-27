package com.pharmacy.orderservice.client;

import com.pharmacy.orderservice.dto.StockCheckResponse;
import com.pharmacy.orderservice.dto.MedicineInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "catalog-service")
public interface CatalogClient {

    @GetMapping("/api/catalog/medicines/{id}")
    MedicineInfo getMedicineById(@PathVariable("id") Long id);

    @GetMapping("/api/catalog/medicines/{id}/stock-check")
    StockCheckResponse checkStock(@PathVariable("id") Long medicineId,
                                  @RequestParam("quantity") Integer quantity);

    // Batch-level stock check — used when adding to cart by batchId
    @GetMapping("/api/catalog/internal/batches/{batchId}/stock-check")
    StockCheckResponse checkBatchStock(@PathVariable("batchId") Long batchId,
                                       @RequestParam("quantity") Integer quantity);

    @PostMapping("/api/catalog/internal/medicines/{id}/deduct")
    void deductStock(@PathVariable("id") Long medicineId,
                     @RequestParam("quantity") Integer quantity);
}
