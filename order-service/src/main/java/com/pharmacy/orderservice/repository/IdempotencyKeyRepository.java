package com.pharmacy.orderservice.repository;



import com.pharmacy.orderservice.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {
    Optional<IdempotencyKey> findByKeyValue(String keyValue);
}