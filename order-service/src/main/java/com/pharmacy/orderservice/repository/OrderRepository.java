package com.pharmacy.orderservice.repository;

import com.pharmacy.orderservice.entity.Order;
import com.pharmacy.orderservice.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserId(Long userId, Pageable pageable);

    Page<Order> findByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);

    Optional<Order> findByOrderNumber(String orderNumber);

    @Query("""
        SELECT o FROM Order o
        WHERE (:status IS NULL OR o.status = :status)
        AND (:userId IS NULL OR o.userId = :userId)
        AND (:dateFrom IS NULL OR o.createdAt >= :dateFrom)
        AND (:dateTo IS NULL OR o.createdAt <= :dateTo)
    """)
    Page<Order> findWithFilters(
            @Param("status") OrderStatus status,
            @Param("userId") Long userId,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable
    );
}
