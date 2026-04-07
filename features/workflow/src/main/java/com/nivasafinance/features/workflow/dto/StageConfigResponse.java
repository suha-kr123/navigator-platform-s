package com.nivasafinance.features.workflow.dto;

import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StageConfigResponse {
    
    private String key;
    
    private String name;
    
    private String description;
    
    private List<PossibleNextStage> possibleNextStages;
    
    private List<String> assigneeRoles;
    
    private List<CodeValueResponse> subStages;
    
    private Boolean isActive;
    
    private LocalDateTime createdAt;
    
    private String createdBy;
    
    private LocalDateTime updatedAt;
    
    private String updatedBy;
}

