package com.pharmacy.orderservice.dto;

import lombok.*;
import java.util.List;

// used when a customer requests a return after delivery
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRequest {
    private String reason;
    private List<Long> itemIds;
}
