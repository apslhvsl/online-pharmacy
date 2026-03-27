package com.pharmacy.catalog.repository;

import com.pharmacy.catalog.entity.InventoryAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryAuditRepository extends JpaRepository<InventoryAudit, Long> {
}
