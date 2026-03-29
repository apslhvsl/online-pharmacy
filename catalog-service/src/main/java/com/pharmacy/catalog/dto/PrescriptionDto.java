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
    private String fileName;
    private String filePath;
    private PrescriptionStatus status;
    private LocalDateTime uploadedAt;
    private LocalDateTime reviewedAt;
    private Long reviewedBy;
    private LocalDateTime validTill;
    private String remarks;
}
