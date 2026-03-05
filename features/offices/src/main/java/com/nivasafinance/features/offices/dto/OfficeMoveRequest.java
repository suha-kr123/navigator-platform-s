package com.nivasafinance.features.offices.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OfficeMoveRequest {
    @Size(max = 255)
    private String newParentKey;
}

