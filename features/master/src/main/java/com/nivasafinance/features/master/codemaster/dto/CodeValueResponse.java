package com.nivasafinance.features.master.codemaster.dto;

import com.nivasafinance.features.master.codemaster.entity.MasterCodeValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeValueResponse {
    private UUID id;
    private String key;
    private String value;
    private String description;
    private Boolean isActive;
    
    public static CodeValueResponse from(MasterCodeValue masterCodeValue) {
        return CodeValueResponse.builder()
                .id(masterCodeValue.getId())
                .key(masterCodeValue.getKey())
                .value(masterCodeValue.getValue() != null && masterCodeValue.getValue().getDefaultValue() != null
                        ? masterCodeValue.getValue().getDefaultValue() : "")
                .description(masterCodeValue.getDescription() != null && masterCodeValue.getDescription().getDefaultValue() != null
                        ? masterCodeValue.getDescription().getDefaultValue() : "")
                .isActive(masterCodeValue.getIsActive())
                .build();
    }
}

