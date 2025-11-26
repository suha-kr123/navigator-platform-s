package com.nivasafinance.features.stage.dto;

import com.nivasafinance.features.stage.entity.StageConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StageFilterResponse {
    
    private String key;
    
    private String name;
    
    private String description;
    
    public static StageFilterResponse from(StageConfig stageConfig) {
        return StageFilterResponse.builder()
                .key(stageConfig.getKey())
                .name(stageConfig.getName())
                .description(stageConfig.getDescription())
                .build();
    }
}

