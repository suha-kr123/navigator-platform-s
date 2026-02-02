package com.nivasafinance.features.bulkoperations.operations.dropoff;

import com.nivasafinance.features.bulkoperations.common.config.BulkOperationCsvProperties;
import com.nivasafinance.features.bulkoperations.common.dto.CsvValidationError;
import com.nivasafinance.features.bulkoperations.common.dto.ValidationOutcome;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationExceptionFactory;
import com.nivasafinance.features.bulkoperations.common.utils.RowDataKeys;
import com.nivasafinance.features.bulkoperations.engine.AbstractBulkOperationCsvValidator;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationType;
import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.enums.LeadSubStatus;
import com.nivasafinance.features.lead.service.LeadReadService;
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService;
import com.nivasafinance.features.master.codemaster.SystemControlledMasterCodes;
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
 * CSV validator for DROPOFF bulk operations.
 * Required columns: lead_identifier, reason_code.
 * Validates: format (UUID, reason code in master), lead exists, and lead is ACTIVE (only active leads can be put on dropoff).
 */
@Component
public class DropoffCsvValidator extends AbstractBulkOperationCsvValidator {

    private final CodeValueMasterService codeValueMasterService;
    private final LeadReadService leadReadService;

    public DropoffCsvValidator(
            BulkOperationCsvProperties bulkOperationCsvProperties,
            BulkOperationExceptionFactory bulkOperationExceptionFactory,
            CodeValueMasterService codeValueMasterService,
            LeadReadService leadReadService) {
        super(bulkOperationCsvProperties, bulkOperationExceptionFactory);
        this.codeValueMasterService = codeValueMasterService;
        this.leadReadService = leadReadService;
    }

    @Override
    public BulkOperationType getType() {
        return BulkOperationType.DROPOFF;
    }

    @Override
    protected List<Map<String, Object>> parseRows(CSVParser parser) {
        List<Map<String, Object>> rows = new ArrayList<>();
        int rowNum = 1;
        for (CSVRecord record : parser) {
            rowNum++;
            Map<String, Object> row = new HashMap<>();
            row.put(RowDataKeys.ROW_NUMBER, rowNum);
            row.put(DropoffRowKeys.LEAD_IDENTIFIER, getString(record, DropoffRowKeys.LEAD_IDENTIFIER));
            row.put(DropoffRowKeys.REASON_CODE, getString(record, DropoffRowKeys.REASON_CODE));
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
        String leadId = getString(row, DropoffRowKeys.LEAD_IDENTIFIER);
        String reasonCode = getString(row, DropoffRowKeys.REASON_CODE);

        Map<String, String> rowDataMap = rowToDataMap(row);
        if (leadId == null || leadId.isBlank()) {
            return CsvValidationError.builder()
                    .rowNumber(rowNum)
                    .columnName(DropoffRowKeys.LEAD_IDENTIFIER)
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
                    .columnName(DropoffRowKeys.LEAD_IDENTIFIER)
                    .errorCode("INVALID_FORMAT")
                    .errorMessage("lead_identifier must be a valid UUID")
                    .rowReference(leadId)
                    .rowData(formatRowData(row))
                    .rowDataMap(rowDataMap)
                    .build();
        }
        if (reasonCode == null || reasonCode.isBlank()) {
            return CsvValidationError.builder()
                    .rowNumber(rowNum)
                    .columnName(DropoffRowKeys.REASON_CODE)
                    .errorCode("REQUIRED")
                    .errorMessage("reason_code is required")
                    .rowReference(leadId)
                    .rowData(formatRowData(row))
                    .rowDataMap(rowDataMap)
                    .build();
        }
        try {
            codeValueMasterService.getCodeValueByKeyAndCodeKey(reasonCode, SystemControlledMasterCodes.LEAD_DROPOFF_REASON_MASTER);
        } catch (Exception e) {
            return CsvValidationError.builder()
                    .rowNumber(rowNum)
                    .columnName(DropoffRowKeys.REASON_CODE)
                    .errorCode("INVALID_REASON")
                    .errorMessage("reason_code '" + reasonCode + "' is not valid")
                    .rowReference(leadId)
                    .rowData(formatRowData(row))
                    .rowDataMap(rowDataMap)
                    .build();
        }
        // Validate lead exists and can transition to dropoff (only ACTIVE leads, not already on dropoff)
        UUID leadIdentifier = UUID.fromString(leadId.trim());
        try {
            var lead = leadReadService.getLeadBasicByIdentifier(leadIdentifier);
            if (lead.getStatus() == null || !LeadStatus.ACTIVE.equals(lead.getStatus())) {
                return CsvValidationError.builder()
                        .rowNumber(rowNum)
                        .columnName(DropoffRowKeys.LEAD_IDENTIFIER)
                        .errorCode("INVALID_STATUS")
                        .errorMessage("Only Active Lead can be put on dropoff")
                        .rowReference(leadId)
                        .rowData(formatRowData(row))
                        .rowDataMap(rowDataMap)
                        .build();
            }
            if (LeadSubStatus.DROPOFF.equals(lead.getSubstatus())) {
                return CsvValidationError.builder()
                        .rowNumber(rowNum)
                        .columnName(DropoffRowKeys.LEAD_IDENTIFIER)
                        .errorCode("ALREADY_DROPOFF")
                        .errorMessage("Lead is already on dropoff")
                        .rowReference(leadId)
                        .rowData(formatRowData(row))
                        .rowDataMap(rowDataMap)
                        .build();
            }
        } catch (ResourceNotFoundException e) {
            return CsvValidationError.builder()
                    .rowNumber(rowNum)
                    .columnName(DropoffRowKeys.LEAD_IDENTIFIER)
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
        map.put(DropoffRowKeys.LEAD_IDENTIFIER, toStringOrEmpty(row.get(DropoffRowKeys.LEAD_IDENTIFIER)));
        map.put(DropoffRowKeys.REASON_CODE, toStringOrEmpty(row.get(DropoffRowKeys.REASON_CODE)));
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
        return "lead_identifier=" + row.get(DropoffRowKeys.LEAD_IDENTIFIER) + ", reason_code=" + row.get(DropoffRowKeys.REASON_CODE);
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
