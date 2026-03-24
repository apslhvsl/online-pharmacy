package com.pharmacy.catalog.dto;

import com.pharmacy.catalog.entity.PrescriptionStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrescriptionDto {
    private Long id;
    private Long userId;
    private String originalFileName;
    private PrescriptionStatus status;
    private LocalDateTime uploadedAt;
}