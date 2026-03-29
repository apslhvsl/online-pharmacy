package com.pharmacy.admin.controller;

import com.pharmacy.admin.dto.DashboardDto;
import com.pharmacy.admin.dto.SalesReportDto;
import com.pharmacy.admin.service.AdminReportService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;

    @Operation(summary = "Get admin dashboard", description = "Returns aggregated KPIs for the admin dashboard including total orders, revenue, active users, and low-stock alerts")
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDto> getDashboard() {
        return ResponseEntity.ok(adminReportService.getDashboard());
    }

    @Operation(summary = "Get sales report", description = "Returns a summary sales report including total revenue, order counts, and top-selling medicines")
    @GetMapping("/reports/sales")
    public ResponseEntity<SalesReportDto> getSalesReport() {
        return ResponseEntity.ok(adminReportService.getSalesReport());
    }
}
