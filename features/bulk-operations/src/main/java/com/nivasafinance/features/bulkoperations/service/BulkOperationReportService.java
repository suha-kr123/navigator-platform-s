package com.nivasafinance.features.bulkoperations.service;

import com.nivasafinance.features.bulkoperations.common.dto.CsvReportRow;
import com.nivasafinance.features.bulkoperations.common.dto.CsvValidationError;
import com.nivasafinance.features.bulkoperations.common.entity.BulkOperation;

import java.util.List;

public interface BulkOperationReportService {

    String buildAndSaveUnifiedReport(
            BulkOperation bulkOperation,
            List<CsvValidationError> validationErrors,
            List<CsvReportRow> successRows,
            List<CsvReportRow> failedRows);
}
