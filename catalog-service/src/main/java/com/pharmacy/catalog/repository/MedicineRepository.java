package com.pharmacy.catalog.repository;

import com.pharmacy.catalog.entity.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    @Query("""
    SELECT m FROM Medicine m
    WHERE (CAST(:name AS string) IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%')))
    AND (CAST(:categoryId AS long) IS NULL OR m.category.id = :categoryId)
""")
    Page<Medicine> findByFilters(
            @Param("name") String name,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );
}