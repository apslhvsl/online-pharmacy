package com.pharmacy.admin.service;

import com.pharmacy.admin.client.CatalogClient;
import com.pharmacy.admin.client.OrderClient;
import com.pharmacy.admin.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final CatalogClient catalogClient;
    private final OrderClient orderClient;

    public DashboardDto getDashboard() {
        DashboardDto orderDashboard = orderClient.getDashboard();
        long lowStockCount = catalogClient.getLowStockMedicines(null).size();
        long expiringCount = catalogClient.getExpiringSoon(null, 90).size();
        long pendingRx = catalogClient.getPendingQueue(null, 0, 1).getTotalElements();

        return new DashboardDto(
                orderDashboard.getTotalOrders(),
                orderDashboard.getTodayRevenue(),
                pendingRx,
                lowStockCount,
                expiringCount,
                orderDashboard.getRecentOrders()
        );
    }

    public SalesReportDto getSalesReport() {
        return orderClient.getSalesReport();
    }
}
