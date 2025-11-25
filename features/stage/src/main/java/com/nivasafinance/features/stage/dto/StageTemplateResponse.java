package com.nivasafinance.features.stage.dto;

import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StageTemplateResponse {
    
    private String stageKey;
    private String stageName;
    private String stageDescription;
    private List<String> possibleNextStages;
    private List<CodeValueResponse> availableSubStages;
}

