package com.pharmacy.catalog.repository;

import com.pharmacy.catalog.entity.InventoryAudit;
import com.pharmacy.catalog.entity.InventoryBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryAuditRepository extends JpaRepository<InventoryAudit, Long> {

    /**
     * Deletes all audit records for a batch before the batch itself is deleted,
     * avoiding the FK constraint violation on inventory_audit.batch_id.
     */
    @Modifying
    @Query("DELETE FROM InventoryAudit a WHERE a.batch = :batch")
    void deleteByBatch(@Param("batch") InventoryBatch batch);
}
