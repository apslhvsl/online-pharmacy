package com.pharmacy.orderservice.dto;

import com.pharmacy.orderservice.entity.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {
    private Long id;
    private Long userId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private Long prescriptionId;
    private String shippingAddress;
    private LocalDateTime createdAt;
    private List<OrderItemDto> items;

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemDto {
        private Long medicineId;
        private String medicineName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }
}