package com.nivasafinance.features.master.location.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TalukaResponse {
    private Long id;
    private String name;
    private String code;
    private Boolean isActive;
    private Integer displayOrder;
}

