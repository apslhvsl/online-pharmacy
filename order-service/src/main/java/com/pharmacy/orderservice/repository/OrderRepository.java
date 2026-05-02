package com.pharmacy.orderservice.repository;

import com.pharmacy.orderservice.entity.Order;
import com.pharmacy.orderservice.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserId(Long userId, Pageable pageable);

    Page<Order> findByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);

    Page<Order> findByUserIdAndStatusIn(Long userId, List<OrderStatus> statuses, Pageable pageable);

    Optional<Order> findByOrderNumber(String orderNumber);

    @Query("""
        SELECT o FROM Order o
        WHERE o.status = :status
        AND o.userId = :userId
        AND o.createdAt >= :dateFrom
        AND o.createdAt <= :dateTo
    """)
    Page<Order> findByStatusAndUserIdAndDateRange(@Param("status") OrderStatus status, @Param("userId") Long userId, @Param("dateFrom") LocalDateTime dateFrom, @Param("dateTo") LocalDateTime dateTo, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.userId = :userId AND o.createdAt >= :dateFrom")
    Page<Order> findByStatusAndUserIdAndDateFrom(@Param("status") OrderStatus status, @Param("userId") Long userId, @Param("dateFrom") LocalDateTime dateFrom, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.userId = :userId AND o.createdAt <= :dateTo")
    Page<Order> findByStatusAndUserIdAndDateTo(@Param("status") OrderStatus status, @Param("userId") Long userId, @Param("dateTo") LocalDateTime dateTo, Pageable pageable);

    Page<Order> findByStatusAndUserId(OrderStatus status, Long userId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt >= :dateFrom AND o.createdAt <= :dateTo")
    Page<Order> findByStatusAndDateRange(@Param("status") OrderStatus status, @Param("dateFrom") LocalDateTime dateFrom, @Param("dateTo") LocalDateTime dateTo, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt >= :dateFrom")
    Page<Order> findByStatusAndDateFrom(@Param("status") OrderStatus status, @Param("dateFrom") LocalDateTime dateFrom, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt <= :dateTo")
    Page<Order> findByStatusAndDateTo(@Param("status") OrderStatus status, @Param("dateTo") LocalDateTime dateTo, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.createdAt >= :dateFrom AND o.createdAt <= :dateTo")
    Page<Order> findByUserIdAndDateRange(@Param("userId") Long userId, @Param("dateFrom") LocalDateTime dateFrom, @Param("dateTo") LocalDateTime dateTo, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.createdAt >= :dateFrom")
    Page<Order> findByUserIdAndDateFrom(@Param("userId") Long userId, @Param("dateFrom") LocalDateTime dateFrom, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.createdAt <= :dateTo")
    Page<Order> findByUserIdAndDateTo(@Param("userId") Long userId, @Param("dateTo") LocalDateTime dateTo, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.createdAt >= :dateFrom AND o.createdAt <= :dateTo")
    Page<Order> findByDateRange(@Param("dateFrom") LocalDateTime dateFrom, @Param("dateTo") LocalDateTime dateTo, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.createdAt >= :dateFrom")
    Page<Order> findByDateFrom(@Param("dateFrom") LocalDateTime dateFrom, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.createdAt <= :dateTo")
    Page<Order> findByDateTo(@Param("dateTo") LocalDateTime dateTo, Pageable pageable);

    @Query("""
        SELECT COALESCE(SUM(o.totalAmount), 0)
        FROM Order o
        WHERE o.status = :status
        AND o.createdAt >= :dateFrom
        AND o.createdAt < :dateTo
    """)
    BigDecimal sumTotalAmountByStatusAndDateRange(
            @Param("status") OrderStatus status,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo
    );
}
