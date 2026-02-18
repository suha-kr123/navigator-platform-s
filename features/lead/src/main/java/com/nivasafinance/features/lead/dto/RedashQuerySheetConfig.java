package com.nivasafinance.features.lead.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for parsing CB config REDASH_CB_REPORT_SHEETS (JSON array of { queryId, sheetName }).
 * Used by lead to obtain Redash query IDs to pass to the Redash module.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedashQuerySheetConfig {
    private Long queryId;
    private String sheetName;
}
