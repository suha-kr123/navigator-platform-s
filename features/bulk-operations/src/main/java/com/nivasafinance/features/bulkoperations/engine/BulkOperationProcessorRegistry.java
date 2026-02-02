package com.nivasafinance.features.bulkoperations.engine;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationExceptionFactory;

import org.springframework.stereotype.Component;

@Component
public class BulkOperationProcessorRegistry {

    private final Map<BulkOperationType, BulkOperationProcessor> processors;
    private final BulkOperationExceptionFactory bulkOperationExceptionFactory;

    public BulkOperationProcessorRegistry(
            List<BulkOperationProcessor> processors,
            BulkOperationExceptionFactory bulkOperationExceptionFactory) {
        this.processors = processors.stream()
                .collect(Collectors.toMap(BulkOperationProcessor::getType, Function.identity(),
                        (a, b) -> {
                            throw bulkOperationExceptionFactory.duplicateProcessorException(a.getType());
                        }));
        this.bulkOperationExceptionFactory = bulkOperationExceptionFactory;
    }

    public BulkOperationProcessor getProcessor(BulkOperationType operationType) {
        BulkOperationProcessor processor = processors.get(operationType);
        if (!ValidationUtils.isNonNull(processor)) {
            throw bulkOperationExceptionFactory.bulkOperationProcessorNotFoundException(operationType);
        }
        return processor;
    }

    public boolean contains(BulkOperationType operationType) {
        return processors.containsKey(operationType);
    }
}
