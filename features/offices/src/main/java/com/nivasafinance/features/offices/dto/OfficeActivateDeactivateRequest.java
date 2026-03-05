package com.nivasafinance.features.offices.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OfficeActivateDeactivateRequest {
    @NotNull
    private Boolean active;
}
