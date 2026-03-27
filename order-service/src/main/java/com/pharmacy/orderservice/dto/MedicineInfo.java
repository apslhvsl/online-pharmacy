package com.pharmacy.orderservice.dto;

import lombok.*;
import java.math.BigDecimal;

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
