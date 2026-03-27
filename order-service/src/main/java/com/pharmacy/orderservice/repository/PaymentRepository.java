package com.pharmacy.orderservice.repository;

import com.pharmacy.orderservice.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
    Optional<Payment> findByGatewayTxnRef(String gatewayTxnRef);
}
