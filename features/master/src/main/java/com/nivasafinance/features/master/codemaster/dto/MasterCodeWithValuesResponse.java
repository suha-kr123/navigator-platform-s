package com.nivasafinance.features.master.codemaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterCodeWithValuesResponse {
    private Long id;
    private String key;
    private String name;
    private String description;
    private Map<String, String> nameMap;
    private Map<String, String> descriptionMap;
    private Boolean isSystemDefined;
    private Long parentId;
    private List<CodeValueResponse> values;
}

