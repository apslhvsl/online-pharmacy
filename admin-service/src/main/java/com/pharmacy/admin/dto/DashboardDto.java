package com.pharmacy.admin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {
    private long totalOrders;
    private BigDecimal todayRevenue;
    private long pendingPrescriptions;
    private long lowStockCount;
    private long expiringCount;
    private List<OrderResponse> recentOrders;
}
