package com.pharmacy.catalog.repository;

import com.pharmacy.catalog.entity.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    @Query("""
        SELECT m FROM Medicine m
        WHERE m.active = true
        AND (:q IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(m.brandName) LIKE LOWER(CONCAT('%', :q, '%')))
        AND (:categoryId IS NULL OR m.category.id = :categoryId)
        AND (:requiresPrescription IS NULL OR m.requiresPrescription = :requiresPrescription)
        AND (:minPrice IS NULL OR m.price >= :minPrice)
        AND (:maxPrice IS NULL OR m.price <= :maxPrice)
    """)
    Page<Medicine> findByFilters(
            @Param("q") String q,
            @Param("categoryId") Long categoryId,
            @Param("requiresPrescription") Boolean requiresPrescription,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    /** Admin variant — does not filter by active */
    @Query("""
        SELECT m FROM Medicine m
        WHERE (:q IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(m.brandName) LIKE LOWER(CONCAT('%', :q, '%')))
        AND (:categoryId IS NULL OR m.category.id = :categoryId)
        AND (:requiresPrescription IS NULL OR m.requiresPrescription = :requiresPrescription)
        AND (:minPrice IS NULL OR m.price >= :minPrice)
        AND (:maxPrice IS NULL OR m.price <= :maxPrice)
    """)
    Page<Medicine> findByFiltersAdmin(
            @Param("q") String q,
            @Param("categoryId") Long categoryId,
            @Param("requiresPrescription") Boolean requiresPrescription,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    List<Medicine> findByIsFeaturedTrueAndActiveTrue();
}
