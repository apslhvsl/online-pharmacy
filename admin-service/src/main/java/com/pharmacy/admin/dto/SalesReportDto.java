package com.pharmacy.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesReportDto {
    private long totalOrdersCompleted;
    private BigDecimal totalRevenue;
    private List<MedicineSalesSummary> topMedicines;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicineSalesSummary {
        private Long medicineId;
        private String medicineName;
        private int totalQuantitySold;
        private BigDecimal totalRevenue;
    }
}