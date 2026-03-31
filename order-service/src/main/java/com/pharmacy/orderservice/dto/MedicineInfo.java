package com.pharmacy.orderservice.dto;

import lombok.*;
import java.math.BigDecimal;

// snapshot of medicine info fetched from catalog-service when adding to cart
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicineInfo {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private Boolean requiresPrescription;
    private Boolean active;
}
