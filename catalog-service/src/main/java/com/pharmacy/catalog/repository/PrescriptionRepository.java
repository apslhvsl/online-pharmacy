package com.pharmacy.catalog.repository;

import com.pharmacy.catalog.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {}