package com.pharmacy.admin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionResponse {
    private Long id;
    private Long userId;
    private String fileName;
    private String fileType;
    private Long fileSizeBytes;
    private String status;
    private Long reviewedBy;
    private String remarks;
    private LocalDateTime validTill;
    private LocalDateTime uploadedAt;
    private LocalDateTime reviewedAt;
}
