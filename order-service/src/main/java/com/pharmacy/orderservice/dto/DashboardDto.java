package com.pharmacy.orderservice.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDto {
    private long totalOrders;
    private BigDecimal todayRevenue;
    private List<OrderDto> recentOrders;
}
