package com.pharmacy.orderservice.repository;


import com.pharmacy.orderservice.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {}