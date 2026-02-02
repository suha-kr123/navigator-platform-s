package com.nivasafinance.features.bulkoperations.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CsvValidationError {
    private Integer rowNumber;
    private String columnName;
    private String errorCode;
    private String errorMessage;
    private String rowData;
    private String rowReference;
}
