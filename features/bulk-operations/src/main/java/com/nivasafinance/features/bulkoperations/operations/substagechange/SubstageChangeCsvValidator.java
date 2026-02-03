package com.nivasafinance.features.bulkoperations.operations.substagechange;

import com.nivasafinance.common.exception.ResourceNotFoundException;
import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.bulkoperations.common.config.BulkOperationCsvProperties;
import com.nivasafinance.features.bulkoperations.common.dto.CsvValidationError;
import com.nivasafinance.features.bulkoperations.common.dto.ValidationOutcome;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationExceptionFactory;
import com.nivasafinance.features.bulkoperations.common.utils.RowDataKeys;
import com.nivasafinance.features.bulkoperations.engine.AbstractBulkOperationCsvValidator;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationType;
import com.nivasafinance.features.lead.service.LeadReadService;
import com.nivasafinance.features.workflow.dto.StageConfigResponse;
import com.nivasafinance.features.workflow.orchestrator.WorkflowOrchestratorService;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * CSV validator for SUBSTAGE_CHANGE bulk operations.
 * Required columns: lead_identifier, stage_key, substage_key.
 * Validates: format (UUID), lead exists, stage exists, substage is valid for the stage.
 */
@Component
public class SubstageChangeCsvValidator extends AbstractBulkOperationCsvValidator {

    private final LeadReadService leadReadService;
    private final WorkflowOrchestratorService workflowOrchestratorService;

    public SubstageChangeCsvValidator(
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
        return BulkOperationType.SUBSTAGE_CHANGE;
    }

    @Override
    protected List<Map<String, Object>> parseRows(CSVParser parser) {
        List<Map<String, Object>> rows = new ArrayList<>();
        int rowNum = 1;
        for (CSVRecord record : parser) {
            rowNum++;
            Map<String, Object> row = new HashMap<>();
            row.put(RowDataKeys.ROW_NUMBER, rowNum);
            row.put(SubstageChangeRowKeys.LEAD_IDENTIFIER, getString(record, SubstageChangeRowKeys.LEAD_IDENTIFIER));
            row.put(SubstageChangeRowKeys.STAGE_KEY, getString(record, SubstageChangeRowKeys.STAGE_KEY));
            row.put(SubstageChangeRowKeys.SUBSTAGE_KEY, getString(record, SubstageChangeRowKeys.SUBSTAGE_KEY));
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
        Integer rowNum = getRowNumber(row);
        String leadId = getString(row, SubstageChangeRowKeys.LEAD_IDENTIFIER);
        String stageKey = getString(row, SubstageChangeRowKeys.STAGE_KEY);
        String substageKey = getString(row, SubstageChangeRowKeys.SUBSTAGE_KEY);

        Map<String, String> rowDataMap = rowToDataMap(row);
        if (leadId == null || leadId.isBlank()) {
            return CsvValidationError.builder()
                    .rowNumber(rowNum)
                    .columnName(SubstageChangeRowKeys.LEAD_IDENTIFIER)
                    .errorCode("REQUIRED")
                    .errorMessage("lead_identifier is required")
                    .rowReference(null)
                    .rowData(formatRowData(row))
                    .rowDataMap(rowDataMap)
                    .build();
        }
        try {
            UUID.fromString(leadId.trim());
        } catch (IllegalArgumentException e) {
            return CsvValidationError.builder()
                    .rowNumber(rowNum)
                    .columnName(SubstageChangeRowKeys.LEAD_IDENTIFIER)
                    .errorCode("INVALID_FORMAT")
                    .errorMessage("lead_identifier must be a valid UUID")
                    .rowReference(leadId)
                    .rowData(formatRowData(row))
                    .rowDataMap(rowDataMap)
                    .build();
        }
        if (stageKey == null || stageKey.isBlank()) {
            return CsvValidationError.builder()
                    .rowNumber(rowNum)
                    .columnName(SubstageChangeRowKeys.STAGE_KEY)
                    .errorCode("REQUIRED")
                    .errorMessage("stage_key is required")
                    .rowReference(leadId)
                    .rowData(formatRowData(row))
                    .rowDataMap(rowDataMap)
                    .build();
        }
        if (substageKey == null || substageKey.isBlank()) {
            return CsvValidationError.builder()
                    .rowNumber(rowNum)
                    .columnName(SubstageChangeRowKeys.SUBSTAGE_KEY)
                    .errorCode("REQUIRED")
                    .errorMessage("substage_key is required")
                    .rowReference(leadId)
                    .rowData(formatRowData(row))
                    .rowDataMap(rowDataMap)
                    .build();
        }
        try {
            StageConfigResponse stageConfig = workflowOrchestratorService.getStageConfig(stageKey);
            boolean isValidSubStage = ValidationUtils.isNonNull(stageConfig.getSubStages())
                    && stageConfig.getSubStages().stream()
                    .anyMatch(subStage -> substageKey.equals(subStage.getKey()));
            if (!isValidSubStage) {
                return CsvValidationError.builder()
                        .rowNumber(rowNum)
                        .columnName(SubstageChangeRowKeys.SUBSTAGE_KEY)
                        .errorCode("INVALID_SUBSTAGE")
                        .errorMessage("substage_key '" + substageKey + "' is not valid for stage '" + stageKey + "'")
                        .rowReference(leadId)
                        .rowData(formatRowData(row))
                        .rowDataMap(rowDataMap)
                        .build();
            }
        } catch (Exception e) {
            return CsvValidationError.builder()
                    .rowNumber(rowNum)
                    .columnName(SubstageChangeRowKeys.STAGE_KEY)
                    .errorCode("INVALID_STAGE")
                    .errorMessage("stage_key '" + stageKey + "' is not valid or substage validation failed: " + e.getMessage())
                    .rowReference(leadId)
                    .rowData(formatRowData(row))
                    .rowDataMap(rowDataMap)
                    .build();
        }
        UUID leadIdentifier = UUID.fromString(leadId.trim());
        try {
            leadReadService.getLeadBasicByIdentifier(leadIdentifier);
        } catch (ResourceNotFoundException e) {
            return CsvValidationError.builder()
                    .rowNumber(rowNum)
                    .columnName(SubstageChangeRowKeys.LEAD_IDENTIFIER)
                    .errorCode("NOT_FOUND")
                    .errorMessage("Lead not found")
                    .rowReference(leadId)
                    .rowData(formatRowData(row))
                    .rowDataMap(rowDataMap)
                    .build();
        }
        return null;
    }

    private static Map<String, String> rowToDataMap(Map<String, Object> row) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put(SubstageChangeRowKeys.LEAD_IDENTIFIER, toStringOrEmpty(row.get(SubstageChangeRowKeys.LEAD_IDENTIFIER)));
        map.put(SubstageChangeRowKeys.STAGE_KEY, toStringOrEmpty(row.get(SubstageChangeRowKeys.STAGE_KEY)));
        map.put(SubstageChangeRowKeys.SUBSTAGE_KEY, toStringOrEmpty(row.get(SubstageChangeRowKeys.SUBSTAGE_KEY)));
        return map;
    }

    private static String toStringOrEmpty(Object v) {
        return v != null ? v.toString().trim() : "";
    }

    private static Integer getRowNumber(Map<String, Object> row) {
        Object v = row.get(RowDataKeys.ROW_NUMBER);
        if (v == null) return null;
        if (v instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(v.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String formatRowData(Map<String, Object> row) {
        return "lead_identifier=" + row.get(SubstageChangeRowKeys.LEAD_IDENTIFIER)
                + ", stage_key=" + row.get(SubstageChangeRowKeys.STAGE_KEY)
                + ", substage_key=" + row.get(SubstageChangeRowKeys.SUBSTAGE_KEY);
    }

    private static String getString(CSVRecord record, String key) {
        try {
            String v = record.get(key);
            return (v != null && !v.isBlank()) ? v.trim() : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String getString(Map<String, Object> map, String key) {
        Object v = map.get(key);
        if (v == null || "".equals(v)) return null;
        String s = v.toString().trim();
        return s.isEmpty() ? null : s;
    }
}
