package com.pharmacy.catalog.repository;

import com.pharmacy.catalog.entity.Prescription;
import com.pharmacy.catalog.entity.PrescriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    List<Prescription> findByUserId(Long userId);

    Page<Prescription> findByStatus(PrescriptionStatus status, Pageable pageable);

    @Query("""
        SELECT p FROM Prescription p
        WHERE (:status IS NULL OR p.status = :status)
        AND (:userId IS NULL OR p.userId = :userId)
        AND (:dateFrom IS NULL OR p.uploadedAt >= :dateFrom)
        AND (:dateTo IS NULL OR p.uploadedAt <= :dateTo)
    """)
    Page<Prescription> findWithFilters(
            @Param("status") PrescriptionStatus status,
            @Param("userId") Long userId,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable
    );
}
