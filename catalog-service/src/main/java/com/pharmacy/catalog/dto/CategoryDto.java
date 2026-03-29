package com.pharmacy.catalog.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDto {
    private Long id;
    private String name;
    private String slug;
    private Boolean active;
    private Integer medicineCount;
}
