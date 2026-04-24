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

    Page<Prescription> findByStatusAndUserId(PrescriptionStatus status, Long userId, Pageable pageable);

    @Query("SELECT p FROM Prescription p WHERE p.status = :status AND p.uploadedAt >= :dateFrom AND p.uploadedAt <= :dateTo")
    Page<Prescription> findByStatusAndDateRange(@Param("status") PrescriptionStatus status, @Param("dateFrom") LocalDateTime dateFrom, @Param("dateTo") LocalDateTime dateTo, Pageable pageable);

    @Query("SELECT p FROM Prescription p WHERE p.status = :status AND p.uploadedAt >= :dateFrom")
    Page<Prescription> findByStatusAndDateFrom(@Param("status") PrescriptionStatus status, @Param("dateFrom") LocalDateTime dateFrom, Pageable pageable);

    @Query("SELECT p FROM Prescription p WHERE p.status = :status AND p.uploadedAt <= :dateTo")
    Page<Prescription> findByStatusAndDateTo(@Param("status") PrescriptionStatus status, @Param("dateTo") LocalDateTime dateTo, Pageable pageable);

    @Query("SELECT p FROM Prescription p WHERE p.status = :status AND p.userId = :userId AND p.uploadedAt >= :dateFrom AND p.uploadedAt <= :dateTo")
    Page<Prescription> findByStatusAndUserIdAndDateRange(@Param("status") PrescriptionStatus status, @Param("userId") Long userId, @Param("dateFrom") LocalDateTime dateFrom, @Param("dateTo") LocalDateTime dateTo, Pageable pageable);

    @Query("SELECT p FROM Prescription p WHERE p.status = :status AND p.userId = :userId AND p.uploadedAt >= :dateFrom")
    Page<Prescription> findByStatusAndUserIdAndDateFrom(@Param("status") PrescriptionStatus status, @Param("userId") Long userId, @Param("dateFrom") LocalDateTime dateFrom, Pageable pageable);

    @Query("SELECT p FROM Prescription p WHERE p.status = :status AND p.userId = :userId AND p.uploadedAt <= :dateTo")
    Page<Prescription> findByStatusAndUserIdAndDateTo(@Param("status") PrescriptionStatus status, @Param("userId") Long userId, @Param("dateTo") LocalDateTime dateTo, Pageable pageable);

    // no-status variants
    Page<Prescription> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT p FROM Prescription p WHERE p.uploadedAt >= :dateFrom AND p.uploadedAt <= :dateTo")
    Page<Prescription> findByDateRange(@Param("dateFrom") LocalDateTime dateFrom, @Param("dateTo") LocalDateTime dateTo, Pageable pageable);

    @Query("SELECT p FROM Prescription p WHERE p.uploadedAt >= :dateFrom")
    Page<Prescription> findByDateFrom(@Param("dateFrom") LocalDateTime dateFrom, Pageable pageable);

    @Query("SELECT p FROM Prescription p WHERE p.uploadedAt <= :dateTo")
    Page<Prescription> findByDateTo(@Param("dateTo") LocalDateTime dateTo, Pageable pageable);

    @Query("SELECT p FROM Prescription p WHERE p.userId = :userId AND p.uploadedAt >= :dateFrom AND p.uploadedAt <= :dateTo")
    Page<Prescription> findByUserIdAndDateRange(@Param("userId") Long userId, @Param("dateFrom") LocalDateTime dateFrom, @Param("dateTo") LocalDateTime dateTo, Pageable pageable);

    @Query("SELECT p FROM Prescription p WHERE p.userId = :userId AND p.uploadedAt >= :dateFrom")
    Page<Prescription> findByUserIdAndDateFrom(@Param("userId") Long userId, @Param("dateFrom") LocalDateTime dateFrom, Pageable pageable);

    @Query("SELECT p FROM Prescription p WHERE p.userId = :userId AND p.uploadedAt <= :dateTo")
    Page<Prescription> findByUserIdAndDateTo(@Param("userId") Long userId, @Param("dateTo") LocalDateTime dateTo, Pageable pageable);
}
