package com.pharmacy.orderservice.repository;

import com.pharmacy.orderservice.entity.OrderStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusLogRepository extends JpaRepository<OrderStatusLog, Long> {
}
