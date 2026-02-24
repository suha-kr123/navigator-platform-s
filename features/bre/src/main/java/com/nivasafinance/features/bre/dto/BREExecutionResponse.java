package com.nivasafinance.features.bre.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BREExecutionResponse {
    private Long logId;
    private Map<String, Object> response;
    private String error;
    private Map<String, Object> request;
}
