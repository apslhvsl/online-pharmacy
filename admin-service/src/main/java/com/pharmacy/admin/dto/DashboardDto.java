package com.pharmacy.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {
    private long totalMedicines;
    private long lowStockCount;
    private long pendingOrdersCount;
    private long totalOrdersCount;
    private List<MedicineResponse> lowStockMedicines;
}