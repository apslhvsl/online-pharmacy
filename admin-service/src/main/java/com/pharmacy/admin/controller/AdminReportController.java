package com.pharmacy.admin.controller;

import com.pharmacy.admin.dto.DashboardDto;
import com.pharmacy.admin.dto.SalesReportDto;
import com.pharmacy.admin.service.AdminReportService;
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

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDto> getDashboard() {
        return ResponseEntity.ok(adminReportService.getDashboard());
    }

    @GetMapping("/reports/sales")
    public ResponseEntity<SalesReportDto> getSalesReport() {
        return ResponseEntity.ok(adminReportService.getSalesReport());
    }
}
