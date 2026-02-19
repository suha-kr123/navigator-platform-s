package com.nivasafinance.features.master.codemaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterCodeSearchResponse {

    private String key;
    private String codeKey;
    private String displayText;
    private String description;
    private Long parentId;
    private Boolean isActive;
}
