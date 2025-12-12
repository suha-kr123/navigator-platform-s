package com.nivasafinance.features.stage.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StageKeysRequest {
    
    @NotEmpty(message = "Stage keys list cannot be empty")
    private List<String> stageKeys;
}

