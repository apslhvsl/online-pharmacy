package com.pharmacy.orderservice.dto;

import lombok.*;

// reason provided by the customer when cancelling an order
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CancelRequest {
    private String reason;
}
