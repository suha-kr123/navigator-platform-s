package com.nivasafinance.features.bulkoperations.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CsvValidationResult {
    @Builder.Default
    private boolean isValid = true;

    @Builder.Default
    private List<CsvValidationError> errors = new ArrayList<>();

    @Builder.Default
    private List<Map<String, Object>> validRows = new ArrayList<>();

    private Integer totalRows;
    private Integer validRowCount;
    private Integer errorRowCount;

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    public static CsvValidationResult error(String message) {
        CsvValidationResult result = new CsvValidationResult();
        result.setValid(false);
        result.getErrors().add(CsvValidationError.builder()
                .errorCode("FILE_ERROR")
                .errorMessage(message)
                .build());
        return result;
    }

    public static CsvValidationResult success(List<Map<String, Object>> validRows, List<CsvValidationError> errors,
            Integer totalRows, Integer validRowCount, Integer errorRowCount, Map<String, Object> metadata) {
        boolean canProceed = validRows != null && !validRows.isEmpty();
        return CsvValidationResult.builder()
                .isValid(canProceed)
                .validRows(validRows != null ? validRows : List.of())
                .errors(errors != null ? errors : List.of())
                .totalRows(totalRows)
                .validRowCount(validRowCount)
                .errorRowCount(errorRowCount)
                .metadata(metadata)
                .build();
    }

}
