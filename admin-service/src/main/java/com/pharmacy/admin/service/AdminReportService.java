package com.pharmacy.admin.service;

import com.pharmacy.admin.client.CatalogClient;
import com.pharmacy.admin.client.OrderClient;
import com.pharmacy.admin.dto.DashboardDto;
import com.pharmacy.admin.dto.MedicineResponse;
import com.pharmacy.admin.dto.OrderResponse;
import com.pharmacy.admin.dto.SalesReportDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final CatalogClient catalogClient;
    private final OrderClient orderClient;

    private static final int LOW_STOCK_THRESHOLD = 10;

    public DashboardDto getDashboard() {
        List<MedicineResponse> allMedicines = catalogClient.getAllMedicines();
        List<OrderResponse> allOrders = orderClient.getAllOrders();

        List<MedicineResponse> lowStock = allMedicines.stream()
                .filter(m -> m.getStockQuantity() != null && m.getStockQuantity() <= LOW_STOCK_THRESHOLD)
                .toList();

        long pendingOrders = allOrders.stream()
                .filter(o -> "PENDING".equals(o.getStatus()) || "CONFIRMED".equals(o.getStatus()))
                .count();

        return new DashboardDto(
                allMedicines.size(),
                lowStock.size(),
                pendingOrders,
                allOrders.size(),
                lowStock
        );
    }

    public SalesReportDto getSalesReport() {
        List<OrderResponse> allOrders = orderClient.getAllOrders();

        List<String> completedStatuses = List.of("DELIVERED", "SHIPPED", "PROCESSING", "CONFIRMED");

        List<OrderResponse> completedOrders = allOrders.stream()
                .filter(o -> completedStatuses.contains(o.getStatus()))
                .toList();

        BigDecimal totalRevenue = completedOrders.stream()
                .map(OrderResponse::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Aggregate per medicine
        Map<Long, SalesReportDto.MedicineSalesSummary> summaryMap = new HashMap<>();
        for (OrderResponse order : completedOrders) {
            if (order.getItems() == null) continue;
            for (var item : order.getItems()) {
                summaryMap.merge(
                        item.getMedicineId(),
                        new SalesReportDto.MedicineSalesSummary(
                                item.getMedicineId(),
                                item.getMedicineName(),
                                item.getQuantity(),
                                item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                        ),
                        (existing, newEntry) -> new SalesReportDto.MedicineSalesSummary(
                                existing.getMedicineId(),
                                existing.getMedicineName(),
                                existing.getTotalQuantitySold() + newEntry.getTotalQuantitySold(),
                                existing.getTotalRevenue().add(newEntry.getTotalRevenue())
                        )
                );
            }
        }

        List<SalesReportDto.MedicineSalesSummary> topMedicines = new ArrayList<>(summaryMap.values());
        topMedicines.sort((a, b) -> Integer.compare(b.getTotalQuantitySold(), a.getTotalQuantitySold()));

        return new SalesReportDto(completedOrders.size(), totalRevenue, topMedicines);
    }
}