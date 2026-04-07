package com.nivasafinance.features.stage.dto;

import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.stage.entity.StageConfig;
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
    
    private List<String> possibleNextStages;
    
    private List<String> assigneeRoles;
    
    private List<CodeValueResponse> subStages;
    
    private Boolean isActive;
    
    private LocalDateTime createdAt;
    
    private String createdBy;
    
    private LocalDateTime updatedAt;
    
    private String updatedBy;
    
    public static StageConfigResponse from(StageConfig stageConfig, List<String> possibleNextStages,
                                           List<String> assigneeRoles, List<CodeValueResponse> subStages) {
        return StageConfigResponse.builder()
                .key(stageConfig.getKey())
                .name(stageConfig.getName())
                .description(stageConfig.getDescription())
                .possibleNextStages(possibleNextStages)
                .assigneeRoles(assigneeRoles)
                .subStages(subStages)
                .isActive(stageConfig.getIsActive())
                .createdAt(stageConfig.getCreatedAt())
                .createdBy(stageConfig.getCreatedBy())
                .updatedAt(stageConfig.getUpdatedAt())
                .updatedBy(stageConfig.getUpdatedBy())
                .build();
    }
}

