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
    private String fileType;
    private Long fileSizeBytes;
    private PrescriptionStatus status;
    private Long reviewedBy;
    private String remarks;
    private LocalDateTime validTill;
    private LocalDateTime uploadedAt;
    private LocalDateTime reviewedAt;
}
