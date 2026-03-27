package com.pharmacy.orderservice.repository;

import com.pharmacy.orderservice.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartIdAndBatchId(Long cartId, Long batchId);
}
