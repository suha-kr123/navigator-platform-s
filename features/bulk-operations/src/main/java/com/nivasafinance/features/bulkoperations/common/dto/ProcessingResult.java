package com.nivasafinance.features.bulkoperations.common.dto;

import com.nivasafinance.features.bulkoperations.common.enums.RowProcessingStatus;

import java.util.Map;


public record ProcessingResult(
        RowProcessingStatus status,
        String errorMessage,
        Map<String, Object> originalValues,
        Map<String, Object> newValues) {

    public static ProcessingResult success(Map<String, Object> originalValues, Map<String, Object> newValues) {
        return new ProcessingResult(RowProcessingStatus.SUCCESS, null, originalValues, newValues);
    }

    public static ProcessingResult failed(String errorMessage, Map<String, Object> originalValues) {
        return new ProcessingResult(RowProcessingStatus.FAILED, errorMessage, originalValues, null);
    }
}
