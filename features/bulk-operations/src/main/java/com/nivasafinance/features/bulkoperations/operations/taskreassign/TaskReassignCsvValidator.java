package com.nivasafinance.features.bulkoperations.operations.taskreassign;

import com.nivasafinance.features.bulkoperations.common.config.BulkOperationCsvProperties;
import com.nivasafinance.features.bulkoperations.common.dto.CsvValidationError;
import com.nivasafinance.features.bulkoperations.common.dto.ValidationOutcome;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationExceptionFactory;
import com.nivasafinance.features.bulkoperations.engine.AbstractBulkOperationCsvValidator;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationType;
import com.nivasafinance.features.bulkoperations.common.utils.RowDataKeys;
import com.nivasafinance.features.task.repository.TaskRepositoryWrapper;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TaskReassignCsvValidator extends AbstractBulkOperationCsvValidator {

    private final TaskRepositoryWrapper taskRepositoryWrapper;

    public TaskReassignCsvValidator(
            BulkOperationCsvProperties bulkOperationCsvProperties,
            BulkOperationExceptionFactory bulkOperationExceptionFactory,
            TaskRepositoryWrapper taskRepositoryWrapper) {
        super(bulkOperationCsvProperties, bulkOperationExceptionFactory);
        this.taskRepositoryWrapper = taskRepositoryWrapper;
    }

    @Override
    public BulkOperationType getType() {
        return BulkOperationType.TASK_REASSIGN;
    }

    @Override
    protected List<Map<String, Object>> parseRows(CSVParser parser) {
        List<Map<String, Object>> rows = new ArrayList<>();
        int rowNum = 1;
        for (CSVRecord record : parser) {
            rowNum++;
            Map<String, Object> row = new HashMap<>();
            row.put(RowDataKeys.ROW_NUMBER, rowNum);
            row.put(TaskReassignRowKeys.TASK_IDENTIFIER, getString(record, TaskReassignRowKeys.TASK_IDENTIFIER));
            row.put(TaskReassignRowKeys.ASSIGNED_TO, getString(record, TaskReassignRowKeys.ASSIGNED_TO));
            rows.add(row);
        }
        return rows;
    }

    @Override
    protected ValidationOutcome validateBusinessRules(List<Map<String, Object>> parsedRows) {
        List<Map<String, Object>> valid = new ArrayList<>();
        List<CsvValidationError> errors = new ArrayList<>();
        for (Map<String, Object> row : parsedRows) {
            CsvValidationError err = validateRow(row);
            if (err == null) {
                valid.add(row);
            } else {
                errors.add(err);
            }
        }
        return ValidationOutcome.of(valid, errors);
    }

    private CsvValidationError validateRow(Map<String, Object> row) {
        int rowNum = (int) row.getOrDefault(RowDataKeys.ROW_NUMBER, -1);
        String taskIdentifierStr = toString(row.get(TaskReassignRowKeys.TASK_IDENTIFIER));
        String assignedTo = toString(row.get(TaskReassignRowKeys.ASSIGNED_TO));
        Map<String, String> rowDataMap = rowToDataMap(row);

        if (taskIdentifierStr == null || taskIdentifierStr.isBlank()) {
            return error(rowNum, TaskReassignRowKeys.TASK_IDENTIFIER, "REQUIRED", "task_identifier is required", taskIdentifierStr, row, rowDataMap);
        }
        UUID taskIdentifier;
        try {
            taskIdentifier = UUID.fromString(taskIdentifierStr.trim());
        } catch (IllegalArgumentException e) {
            return error(rowNum, TaskReassignRowKeys.TASK_IDENTIFIER, "INVALID_UUID", "task_identifier must be a valid UUID", taskIdentifierStr, row, rowDataMap);
        }

        try {
            // Will throw if not found
            taskRepositoryWrapper.findByTaskIdentifierWithException(taskIdentifier);
        } catch (RuntimeException e) {
            return error(rowNum, TaskReassignRowKeys.TASK_IDENTIFIER, "NOT_FOUND", "Task not found", taskIdentifierStr, row, rowDataMap);
        }

        if (assignedTo == null || assignedTo.isBlank()) {
            return error(rowNum, TaskReassignRowKeys.ASSIGNED_TO, "REQUIRED", "assigned_to is required", taskIdentifierStr, row, rowDataMap);
        }

        return null;
    }

    private static String getString(CSVRecord record, String key) {
        String v = record.isMapped(key) ? record.get(key) : null;
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

    private static String toString(Object v) {
        return v != null ? v.toString() : null;
    }

    private static CsvValidationError error(int rowNum, String column, String code, String message, String rowRef,
                                           Map<String, Object> row, Map<String, String> rowDataMap) {
        return CsvValidationError.builder()
                .rowNumber(rowNum)
                .columnName(column)
                .errorCode(code)
                .errorMessage(message)
                .rowReference(rowRef)
                .rowData(formatRowData(row))
                .rowDataMap(rowDataMap)
                .build();
    }

    private static Map<String, String> rowToDataMap(Map<String, Object> row) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put(TaskReassignRowKeys.TASK_IDENTIFIER, toStringOrEmpty(row.get(TaskReassignRowKeys.TASK_IDENTIFIER)));
        map.put(TaskReassignRowKeys.ASSIGNED_TO, toStringOrEmpty(row.get(TaskReassignRowKeys.ASSIGNED_TO)));
        return map;
    }

    private static String formatRowData(Map<String, Object> row) {
        return "task_identifier=" + toStringOrEmpty(row.get(TaskReassignRowKeys.TASK_IDENTIFIER))
                + ", assigned_to=" + toStringOrEmpty(row.get(TaskReassignRowKeys.ASSIGNED_TO));
    }

    private static String toStringOrEmpty(Object v) {
        return v != null ? v.toString().trim() : "";
    }
}
