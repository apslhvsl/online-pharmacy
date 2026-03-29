package com.pharmacy.catalog.repository;

import com.pharmacy.catalog.entity.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    @Query(value = """
        SELECT * FROM medicines m
        WHERE m.active = true
        AND (CAST(:q AS text) IS NULL OR LOWER(m.name) LIKE LOWER('%' || CAST(:q AS text) || '%'))
        AND (CAST(:categoryId AS bigint) IS NULL OR m.category_id = :categoryId)
        AND (CAST(:requiresPrescription AS boolean) IS NULL OR m.requires_prescription = :requiresPrescription)
        AND (CAST(:minPrice AS numeric) IS NULL OR m.price >= :minPrice)
        AND (CAST(:maxPrice AS numeric) IS NULL OR m.price <= :maxPrice)
    """, nativeQuery = true)
    Page<Medicine> findByFilters(
            @Param("q") String q,
            @Param("categoryId") Long categoryId,
            @Param("requiresPrescription") Boolean requiresPrescription,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    @Query(value = """
        SELECT * FROM medicines m
        WHERE (CAST(:q AS text) IS NULL OR LOWER(m.name) LIKE LOWER('%' || CAST(:q AS text) || '%'))
        AND (CAST(:categoryId AS bigint) IS NULL OR m.category_id = :categoryId)
        AND (CAST(:requiresPrescription AS boolean) IS NULL OR m.requires_prescription = :requiresPrescription)
        AND (CAST(:minPrice AS numeric) IS NULL OR m.price >= :minPrice)
        AND (CAST(:maxPrice AS numeric) IS NULL OR m.price <= :maxPrice)
    """, nativeQuery = true)
    Page<Medicine> findByFiltersAdmin(
            @Param("q") String q,
            @Param("categoryId") Long categoryId,
            @Param("requiresPrescription") Boolean requiresPrescription,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );
}
