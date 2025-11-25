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
public class StageTransitionRequest {
    
    @NotBlank(message = "Stage key is required")
    private String stageKey;
    
    private String previousStageKey;
    
    private String assignedTo;
    
    private String remarks;
}

