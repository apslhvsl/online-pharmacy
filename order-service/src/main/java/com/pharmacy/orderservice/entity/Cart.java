package com.pharmacy.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "carts")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @ElementCollection
    @CollectionTable(name = "cart_items", joinColumns = @JoinColumn(name = "cart_id"))
    @MapKeyColumn(name = "medicine_id")
    @Column(name = "quantity")
    @Builder.Default
    private Map<Long, Integer> items = new HashMap<>();
}