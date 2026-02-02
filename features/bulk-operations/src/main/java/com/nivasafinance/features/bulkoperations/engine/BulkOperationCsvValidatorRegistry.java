package com.nivasafinance.features.bulkoperations.engine;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationExceptionFactory;

import org.springframework.stereotype.Component;

@Component
public class BulkOperationCsvValidatorRegistry {

    private final Map<BulkOperationType, BulkOperationCsvValidator> validators;
    private final BulkOperationExceptionFactory bulkOperationExceptionFactory;

    public BulkOperationCsvValidatorRegistry(
            List<BulkOperationCsvValidator> validators,
            BulkOperationExceptionFactory bulkOperationExceptionFactory) {
        this.validators = validators.stream()
                .collect(Collectors.toMap(BulkOperationCsvValidator::getType, Function.identity(),
                        (a, b) -> {
                            throw bulkOperationExceptionFactory.duplicateValidatorException(a.getType());
                        }));
        this.bulkOperationExceptionFactory = bulkOperationExceptionFactory;
    }

    public BulkOperationCsvValidator getValidator(BulkOperationType operationType) {
        BulkOperationCsvValidator validator = validators.get(operationType);
        if (!ValidationUtils.isNonNull(validator)) {
            throw bulkOperationExceptionFactory.bulkOperationValidatorNotFoundException(operationType);
        }
        return validator;
    }

    public boolean contains(BulkOperationType operationType) {
        return validators.containsKey(operationType);
    }
}
