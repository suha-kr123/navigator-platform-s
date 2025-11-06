package com.nivasafinance.features.master.codemaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterCodeWithValuesResponse {
    private UUID id;
    private String key;
    private String name;
    private String description;
    private Boolean isSystemDefined;
    private UUID parentId;
    private List<CodeValueResponse> values;
}

