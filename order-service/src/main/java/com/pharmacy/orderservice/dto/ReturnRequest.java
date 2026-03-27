package com.pharmacy.orderservice.dto;

import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRequest {
    private String reason;
    private List<Long> itemIds;
}
