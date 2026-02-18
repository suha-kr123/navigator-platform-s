package com.nivasafinance.redash.dto;

import feign.Response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Input for merging multiple Redash XLSX responses into a single workbook.
 * Holds query IDs, responses (in same order), and optional queryId-to-sheet-name mapping.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelMergeInput {

    private List<Long> queryIds;
    private List<Response> responses;
    private Map<Long, String> queryIdToSheetName;
}
