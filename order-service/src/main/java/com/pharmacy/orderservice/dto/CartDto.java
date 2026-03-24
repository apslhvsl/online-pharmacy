package com.pharmacy.orderservice.dto;

import lombok.*;

import java.util.Map;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDto {
    private Long userId;
    private Map<Long, Integer> items; // medicineId -> quantity
}