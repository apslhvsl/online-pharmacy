package com.pharmacy.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_keys")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdempotencyKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String keyValue;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}