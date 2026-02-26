package com.nivasafinance.features.bre.dto;

import com.nivasafinance.common.enums.SystemEntities;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BREExecutionRequest {
    private SystemEntities entity;
    private Long entityId;
    private Map<String, Object> params;
}
