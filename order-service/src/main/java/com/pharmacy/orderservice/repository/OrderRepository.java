package com.pharmacy.orderservice.repository;


import com.pharmacy.orderservice.entity.Order;
import com.pharmacy.orderservice.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);
}