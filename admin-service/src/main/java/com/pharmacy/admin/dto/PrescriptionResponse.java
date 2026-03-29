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
    private String filePath;
    private String status;
    private LocalDateTime uploadedAt;
    private LocalDateTime reviewedAt;
    private Long reviewedBy;
    private LocalDateTime validTill;
    private String remarks;
}
