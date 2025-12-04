package com.nivasafinance.features.lead.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnholdLeadRequest {
    private String reasonCode;
    
    @NotNull(message = "Hold follow-up date is required")
    private LocalDate holdFollowUpDate;
}

