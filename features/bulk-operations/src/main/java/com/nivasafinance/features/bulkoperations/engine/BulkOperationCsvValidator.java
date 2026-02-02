package com.nivasafinance.features.bulkoperations.engine;

import com.nivasafinance.features.bulkoperations.common.dto.CsvValidationResult;

import org.springframework.web.multipart.MultipartFile;

/**
 * Self-describing CSV validator for bulk operations.
 * Implementations declare their type via {@link #getType()}; the registry auto-discovers them.
 */
public interface BulkOperationCsvValidator {

    BulkOperationType getType();

    CsvValidationResult validateCsv(MultipartFile file);
}
