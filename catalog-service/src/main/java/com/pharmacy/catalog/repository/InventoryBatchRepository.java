package com.pharmacy.catalog.repository;

import com.pharmacy.catalog.entity.InventoryBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface InventoryBatchRepository extends JpaRepository<InventoryBatch, Long> {

    List<InventoryBatch> findByMedicineId(Long medicineId);

    /** Non-expired batches for a medicine, ordered by expiry (FEFO) */
    @Query("SELECT b FROM InventoryBatch b WHERE b.medicine.id = :medicineId AND b.expiryDate > :today AND b.quantity > 0 ORDER BY b.expiryDate ASC")
    List<InventoryBatch> findAvailableByMedicineId(@Param("medicineId") Long medicineId, @Param("today") LocalDate today);

    /** Total available stock across non-expired batches */
    @Query("SELECT COALESCE(SUM(b.quantity), 0) FROM InventoryBatch b WHERE b.medicine.id = :medicineId AND b.expiryDate > :today")
    Integer sumAvailableStock(@Param("medicineId") Long medicineId, @Param("today") LocalDate today);

    /** Batches expiring before a given date */
    @Query("SELECT b FROM InventoryBatch b WHERE b.expiryDate < :threshold AND b.quantity > 0 ORDER BY b.expiryDate ASC")
    List<InventoryBatch> findExpiringSoon(@Param("threshold") LocalDate threshold);

    /** Medicines whose total available stock is below a threshold */    @Query("""
        SELECT b.medicine.id FROM InventoryBatch b
        WHERE b.expiryDate > :today
        GROUP BY b.medicine.id
        HAVING COALESCE(SUM(b.quantity), 0) <= :threshold
    """)
    List<Long> findMedicineIdsWithLowStock(@Param("threshold") int threshold, @Param("today") LocalDate today);
}
