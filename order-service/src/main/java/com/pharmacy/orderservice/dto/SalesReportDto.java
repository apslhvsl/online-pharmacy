package com.pharmacy.orderservice.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesReportDto {
    private long totalOrdersCompleted;
    private BigDecimal totalRevenue;
    private List<MedicineSalesSummary> topMedicines;

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MedicineSalesSummary {
        private Long medicineId;
        private String medicineName;
        private int totalQuantitySold;
        private BigDecimal totalRevenue;
    }
}
