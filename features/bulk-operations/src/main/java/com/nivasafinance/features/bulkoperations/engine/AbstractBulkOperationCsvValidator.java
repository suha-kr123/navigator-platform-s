package com.nivasafinance.features.bulkoperations.engine;

import com.nivasafinance.features.bulkoperations.common.config.BulkOperationCsvProperties;
import com.nivasafinance.features.bulkoperations.common.dto.CsvValidationResult;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationExceptionFactory;
import org.springframework.web.multipart.MultipartFile;

/**
 * Bridge between {@link BulkOperationCsvValidator} and {@link BaseCsvValidator}.
 * Subclasses implement {@link #getType()}, {@link #parseRows}, and {@link #validateBusinessRules}.
 */
public abstract class AbstractBulkOperationCsvValidator extends BaseCsvValidator
        implements BulkOperationCsvValidator {

    protected AbstractBulkOperationCsvValidator(
            BulkOperationCsvProperties bulkOperationCsvProperties,
            BulkOperationExceptionFactory bulkOperationExceptionFactory) {
        super(bulkOperationCsvProperties, bulkOperationExceptionFactory);
    }

    @Override
    public CsvValidationResult validateCsv(MultipartFile file) {
        return validate(file, getType().getRequiredColumns());
    }
}
