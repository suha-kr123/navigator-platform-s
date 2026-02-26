package com.nivasafinance.features.bulkoperations.operations.stageassignment;

import com.nivasafinance.common.exception.ResourceNotFoundException;
import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.bulkoperations.common.config.BulkOperationCsvProperties;
import com.nivasafinance.features.bulkoperations.common.dto.CsvValidationError;
import com.nivasafinance.features.bulkoperations.common.dto.ValidationOutcome;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationExceptionFactory;
import com.nivasafinance.features.bulkoperations.engine.AbstractBulkOperationCsvValidator;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationType;
import com.nivasafinance.features.bulkoperations.common.utils.RowDataKeys;
import com.nivasafinance.features.lead.service.LeadReadService;
import com.nivasafinance.features.workflow.orchestrator.WorkflowOrchestratorService;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class StageAssignmentCsvValidator extends AbstractBulkOperationCsvValidator {

    private final LeadReadService leadReadService;
    private final WorkflowOrchestratorService workflowOrchestratorService;

    public StageAssignmentCsvValidator(
            BulkOperationCsvProperties bulkOperationCsvProperties,
            BulkOperationExceptionFactory bulkOperationExceptionFactory,
            LeadReadService leadReadService,
            WorkflowOrchestratorService workflowOrchestratorService) {
        super(bulkOperationCsvProperties, bulkOperationExceptionFactory);
        this.leadReadService = leadReadService;
        this.workflowOrchestratorService = workflowOrchestratorService;
    }

    @Override
    public BulkOperationType getType() {
        return BulkOperationType.STAGE_ASSIGNMENT_CHANGE;
    }

    @Override
    protected List<Map<String, Object>> parseRows(CSVParser parser) {
        List<Map<String, Object>> rows = new ArrayList<>();
        int rowNum = 1; // header is 1; first data row will be 2
        for (CSVRecord record : parser) {
            rowNum++;
            Map<String, Object> row = new HashMap<>();
            row.put(RowDataKeys.ROW_NUMBER, rowNum);
            row.put(StageAssignmentRowKeys.LEAD_IDENTIFIER, getString(record, StageAssignmentRowKeys.LEAD_IDENTIFIER));
            row.put(StageAssignmentRowKeys.STAGE_KEY, getString(record, StageAssignmentRowKeys.STAGE_KEY));
            row.put(StageAssignmentRowKeys.ASSIGNED_TO, getString(record, StageAssignmentRowKeys.ASSIGNED_TO));
            rows.add(row);
        }
        return rows;
    }

    @Override
    protected ValidationOutcome validateBusinessRules(List<Map<String, Object>> parsedRows) {
        List<Map<String, Object>> valid = new ArrayList<>();
        List<CsvValidationError> errors = new ArrayList<>();

        for (Map<String, Object> row : parsedRows) {
            CsvValidationError error = validateRow(row);
            if (error == null) {
                valid.add(row);
            } else {
                errors.add(error);
            }
        }
        return ValidationOutcome.of(valid, errors);
    }

    private CsvValidationError validateRow(Map<String, Object> row) {
        int rowNum = (int) row.getOrDefault(RowDataKeys.ROW_NUMBER, -1);
        String leadIdStr = toString(row.get(StageAssignmentRowKeys.LEAD_IDENTIFIER));
        String stageKey = toString(row.get(StageAssignmentRowKeys.STAGE_KEY));
        String assignedTo = toString(row.get(StageAssignmentRowKeys.ASSIGNED_TO));

        Map<String, String> rowDataMap = rowToDataMap(row);

        if (leadIdStr == null || leadIdStr.isBlank()) {
            return error(rowNum, StageAssignmentRowKeys.LEAD_IDENTIFIER, "REQUIRED", "lead_identifier is required", leadIdStr, row, rowDataMap);
        }
        UUID leadIdentifier;
        try {
            leadIdentifier = UUID.fromString(leadIdStr.trim());
        } catch (IllegalArgumentException e) {
            return error(rowNum, StageAssignmentRowKeys.LEAD_IDENTIFIER, "INVALID_UUID", "lead_identifier must be a valid UUID", leadIdStr, row, rowDataMap);
        }

        try {
            leadReadService.getLeadByIdentifier(leadIdentifier);
        } catch (ResourceNotFoundException e) {
            return error(rowNum, StageAssignmentRowKeys.LEAD_IDENTIFIER, "NOT_FOUND", "Lead not found", leadIdStr, row, rowDataMap);
        }

        if (!ValidationUtils.isNonNullOrEmpty(stageKey)) {
            return error(rowNum, StageAssignmentRowKeys.STAGE_KEY, "REQUIRED", "stage_key is required", leadIdStr, row, rowDataMap);
        }
        try {
            // will throw if not found
            workflowOrchestratorService.getStageConfig(stageKey);
        } catch (Exception e) {
            return error(rowNum, StageAssignmentRowKeys.STAGE_KEY, "INVALID_STAGE", "Invalid stage_key", leadIdStr, row, rowDataMap);
        }

        if (!ValidationUtils.isNonNullOrEmpty(assignedTo)) {
            return error(rowNum, StageAssignmentRowKeys.ASSIGNED_TO, "REQUIRED", "assigned_to is required", leadIdStr, row, rowDataMap);
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
        map.put(StageAssignmentRowKeys.LEAD_IDENTIFIER, toStringOrEmpty(row.get(StageAssignmentRowKeys.LEAD_IDENTIFIER)));
        map.put(StageAssignmentRowKeys.STAGE_KEY, toStringOrEmpty(row.get(StageAssignmentRowKeys.STAGE_KEY)));
        map.put(StageAssignmentRowKeys.ASSIGNED_TO, toStringOrEmpty(row.get(StageAssignmentRowKeys.ASSIGNED_TO)));
        return map;
    }

    private static String formatRowData(Map<String, Object> row) {
        return "lead_identifier=" + toStringOrEmpty(row.get(StageAssignmentRowKeys.LEAD_IDENTIFIER))
                + ", stage_key=" + toStringOrEmpty(row.get(StageAssignmentRowKeys.STAGE_KEY))
                + ", assigned_to=" + toStringOrEmpty(row.get(StageAssignmentRowKeys.ASSIGNED_TO));
    }

    private static String toStringOrEmpty(Object v) {
        return v != null ? v.toString().trim() : "";
    }
}
