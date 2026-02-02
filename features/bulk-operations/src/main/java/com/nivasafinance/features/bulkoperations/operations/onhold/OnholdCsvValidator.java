package com.nivasafinance.features.bulkoperations.operations.onhold;

import com.nivasafinance.features.bulkoperations.common.config.BulkOperationCsvProperties;
import com.nivasafinance.features.bulkoperations.common.dto.CsvValidationError;
import com.nivasafinance.features.bulkoperations.common.dto.ValidationOutcome;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationExceptionFactory;
import com.nivasafinance.features.bulkoperations.common.utils.RowDataKeys;
import com.nivasafinance.features.bulkoperations.engine.AbstractBulkOperationCsvValidator;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationType;
import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.service.LeadReadService;
import com.nivasafinance.features.master.codemaster.SystemControlledMasterCodes;
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.nivasafinance.common.exception.ResourceNotFoundException;

/**
 * CSV validator for ONHOLD bulk operations.
 * Required columns: lead_identifier, reason_code, follow_up_date.
 * Validates: format (UUID, reason code in master, valid date), lead exists, and lead is not rejected/withdrawn.
 */
@Component
public class OnholdCsvValidator extends AbstractBulkOperationCsvValidator {

	private final CodeValueMasterService codeValueMasterService;
	private final LeadReadService leadReadService;

	public OnholdCsvValidator(
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
		return BulkOperationType.ONHOLD;
	}

	@Override
	protected List<Map<String, Object>> parseRows(CSVParser parser) {
		List<Map<String, Object>> rows = new ArrayList<>();
		int rowNum = 1;
		for (CSVRecord record : parser) {
			rowNum++;
			Map<String, Object> row = new HashMap<>();
			row.put(RowDataKeys.ROW_NUMBER, rowNum);
			row.put(OnholdRowKeys.LEAD_IDENTIFIER, getString(record, OnholdRowKeys.LEAD_IDENTIFIER));
			row.put(OnholdRowKeys.REASON_CODE, getString(record, OnholdRowKeys.REASON_CODE));
			row.put(OnholdRowKeys.FOLLOW_UP_DATE, getString(record, OnholdRowKeys.FOLLOW_UP_DATE));
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
		String leadId = getString(row, OnholdRowKeys.LEAD_IDENTIFIER);
		String reasonCode = getString(row, OnholdRowKeys.REASON_CODE);
		String followUpDateStr = getString(row, OnholdRowKeys.FOLLOW_UP_DATE);

		Map<String, String> rowDataMap = rowToDataMap(row);
		if (leadId == null || leadId.isBlank()) {
			return CsvValidationError.builder()
					.rowNumber(rowNum)
					.columnName(OnholdRowKeys.LEAD_IDENTIFIER)
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
					.columnName(OnholdRowKeys.LEAD_IDENTIFIER)
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
					.columnName(OnholdRowKeys.REASON_CODE)
					.errorCode("REQUIRED")
					.errorMessage("reason_code is required")
					.rowReference(leadId)
					.rowData(formatRowData(row))
					.rowDataMap(rowDataMap)
					.build();
		}
		try {
			codeValueMasterService.getCodeValueByKeyAndCodeKey(reasonCode, SystemControlledMasterCodes.LEAD_ONHOLD_REASON_MASTER);
		} catch (Exception e) {
			return CsvValidationError.builder()
					.rowNumber(rowNum)
					.columnName(OnholdRowKeys.REASON_CODE)
					.errorCode("INVALID_REASON")
					.errorMessage("reason_code '" + reasonCode + "' is not valid")
					.rowReference(leadId)
					.rowData(formatRowData(row))
					.rowDataMap(rowDataMap)
					.build();
		}
		if (followUpDateStr == null || followUpDateStr.isBlank()) {
			return CsvValidationError.builder()
					.rowNumber(rowNum)
					.columnName(OnholdRowKeys.FOLLOW_UP_DATE)
					.errorCode("REQUIRED")
					.errorMessage("follow_up_date is required")
					.rowReference(leadId)
					.rowData(formatRowData(row))
					.rowDataMap(rowDataMap)
					.build();
		}
		LocalDate followUpDate;
		try {
			followUpDate = LocalDate.parse(followUpDateStr.trim());
		} catch (DateTimeParseException e) {
			return CsvValidationError.builder()
					.rowNumber(rowNum)
					.columnName(OnholdRowKeys.FOLLOW_UP_DATE)
					.errorCode("INVALID_FORMAT")
					.errorMessage("follow_up_date must be a valid date (yyyy-MM-dd)")
					.rowReference(leadId)
					.rowData(formatRowData(row))
					.rowDataMap(rowDataMap)
					.build();
		}
		// Store parsed date back for processor
		row.put(OnholdRowKeys.FOLLOW_UP_DATE, followUpDate);
		UUID leadIdentifier = UUID.fromString(leadId.trim());
		try {
			var lead = leadReadService.getLeadBasicByIdentifier(leadIdentifier);
			if (LeadStatus.REJECTED.equals(lead.getStatus()) || LeadStatus.WITHDRAWN.equals(lead.getStatus())) {
				return CsvValidationError.builder()
						.rowNumber(rowNum)
						.columnName(OnholdRowKeys.LEAD_IDENTIFIER)
						.errorCode("INVALID_STATUS")
						.errorMessage("Cannot put lead on hold, lead is rejected or withdrawn")
						.rowReference(leadId)
						.rowData(formatRowData(row))
						.rowDataMap(rowDataMap)
						.build();
			}
		} catch (ResourceNotFoundException e) {
			return CsvValidationError.builder()
					.rowNumber(rowNum)
					.columnName(OnholdRowKeys.LEAD_IDENTIFIER)
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
		map.put(OnholdRowKeys.LEAD_IDENTIFIER, toStringOrEmpty(row.get(OnholdRowKeys.LEAD_IDENTIFIER)));
		map.put(OnholdRowKeys.REASON_CODE, toStringOrEmpty(row.get(OnholdRowKeys.REASON_CODE)));
		map.put(OnholdRowKeys.FOLLOW_UP_DATE, toStringOrEmpty(row.get(OnholdRowKeys.FOLLOW_UP_DATE)));
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
		return "lead_identifier=" + row.get(OnholdRowKeys.LEAD_IDENTIFIER)
				+ ", reason_code=" + row.get(OnholdRowKeys.REASON_CODE)
				+ ", follow_up_date=" + row.get(OnholdRowKeys.FOLLOW_UP_DATE);
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
