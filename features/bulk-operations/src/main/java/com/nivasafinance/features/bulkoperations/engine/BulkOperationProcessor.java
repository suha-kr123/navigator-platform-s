package com.nivasafinance.features.bulkoperations.engine;

import com.nivasafinance.features.bulkoperations.common.dto.OperationProcessingResult;
import com.nivasafinance.features.bulkoperations.common.dto.ProcessingResult;
import com.nivasafinance.features.bulkoperations.common.entity.BulkOperation;

import java.util.Map;

/**
 * Self-describing processor for bulk operations.
 * Implementations declare their type via {@link #getType()}; the registry auto-discovers them.
 */
public interface BulkOperationProcessor {

    BulkOperationType getType();

    OperationProcessingResult process(BulkOperation bulkOperation);

    ProcessingResult processRow(BulkOperation bulkOperation, Map<String, Object> row);
}
