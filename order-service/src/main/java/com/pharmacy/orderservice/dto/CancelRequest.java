package com.pharmacy.orderservice.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CancelRequest {
    private String reason;
}
