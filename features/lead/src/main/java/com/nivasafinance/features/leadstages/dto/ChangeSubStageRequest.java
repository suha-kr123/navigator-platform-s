package com.nivasafinance.features.leadstages.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeSubStageRequest {
    
    @NotBlank(message = "Stage key is required")
    private String stageKey;
    
    @NotBlank(message = "Sub stage key is required")
    private String subStageKey;
}

