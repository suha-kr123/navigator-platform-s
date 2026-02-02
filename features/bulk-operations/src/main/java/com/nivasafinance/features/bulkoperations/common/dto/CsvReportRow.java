package com.nivasafinance.features.bulkoperations.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CsvReportRow {

    private int rowNumber;
    private String rowReference;
    private String errorMessage;
    private Map<String, Object> originalValues;
    private Map<String, Object> newValues;
    private Map<String, String> rowData;
}
