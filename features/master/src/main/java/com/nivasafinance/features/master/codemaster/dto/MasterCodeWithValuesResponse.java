package com.nivasafinance.features.master.codemaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterCodeWithValuesResponse {
    private Long id;
    private String key;
    private String name;
    private String description;
    private Boolean isSystemDefined;
    private Long parentId;
    private List<CodeValueResponse> values;
}

