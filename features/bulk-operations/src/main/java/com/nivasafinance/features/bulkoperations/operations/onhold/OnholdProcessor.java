package com.nivasafinance.features.bulkoperations.operations.onhold;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.features.bulkoperations.common.config.BulkOperationCsvProperties;
import com.nivasafinance.features.bulkoperations.common.config.BulkOperationProcessingProperties;
import com.nivasafinance.features.bulkoperations.common.dto.ProcessingResult;
import com.nivasafinance.features.bulkoperations.common.entity.BulkOperation;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationExceptionFactory;
import com.nivasafinance.features.bulkoperations.engine.BaseBulkOperationProcessor;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationType;
import com.nivasafinance.features.bulkoperations.repository.BulkOperationRepository;
import com.nivasafinance.features.bulkoperations.service.BulkOperationProcessingPersistence;
import com.nivasafinance.features.bulkoperations.service.BulkOperationReportService;
import com.nivasafinance.features.bulkoperations.service.BulkOperationRowTransactionRunner;
import com.nivasafinance.features.bulkoperations.storage.BulkOperationFileStorageService;
import com.nivasafinance.features.lead.dto.OnholdLeadRequest;
import com.nivasafinance.features.lead.service.LeadWriteService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Processor for ONHOLD bulk operations. Calls LeadWriteService.onholdLead for each row.
 * Dry-run skips actual domain calls and returns simulated success.
 */
@Component
public class OnholdProcessor extends BaseBulkOperationProcessor {

	private static final String STATUS = "status";
	private static final String ONHOLD = "ONHOLD";

	private final LeadWriteService leadWriteService;

	public OnholdProcessor(
			BulkOperationReportService reportService,
			BulkOperationFileStorageService fileStorageService,
			BulkOperationRepository bulkOperationRepository,
			BulkOperationProcessingPersistence persistence,
			BulkOperationRowTransactionRunner rowTransactionRunner,
			BulkOperationCsvProperties csvProperties,
			BulkOperationExceptionFactory exceptionFactory,
			BulkOperationProcessingProperties processingProperties,
			LeadWriteService leadWriteService) {
		super(reportService, fileStorageService, bulkOperationRepository, persistence, rowTransactionRunner, csvProperties, exceptionFactory, processingProperties.getBatchSize());
		this.leadWriteService = leadWriteService;
	}

	@Override
	public BulkOperationType getType() {
		return BulkOperationType.ONHOLD;
	}

	@Override
	public ProcessingResult processRow(BulkOperation bulkOperation, Map<String, Object> row) {
		UUID leadId = getUuid(row, OnholdRowKeys.LEAD_IDENTIFIER);
		String reasonCode = getString(row, OnholdRowKeys.REASON_CODE);
		LocalDate followUpDate = getFollowUpDate(row);

		Map<String, Object> original = Map.of(
				OnholdRowKeys.LEAD_IDENTIFIER, leadId != null ? leadId.toString() : "",
				OnholdRowKeys.REASON_CODE, reasonCode != null ? reasonCode : "",
				OnholdRowKeys.FOLLOW_UP_DATE, followUpDate != null ? followUpDate.toString() : "");

		if (leadId == null || reasonCode == null || reasonCode.isBlank() || followUpDate == null) {
			return ProcessingResult.failed(OnholdRowKeys.LEAD_IDENTIFIER + ", " + OnholdRowKeys.REASON_CODE + " and " + OnholdRowKeys.FOLLOW_UP_DATE + " are required", original);
		}

		if (Boolean.TRUE.equals(bulkOperation.getIsDryRun())) {
			Map<String, Object> newValues = new HashMap<>(original);
			newValues.put(STATUS, ONHOLD + " (dry-run)");
			return ProcessingResult.success(original, newValues);
		}

		try {
			OnholdLeadRequest request = OnholdLeadRequest.builder()
					.reasonCode(reasonCode)
					.holdFollowUpDate(followUpDate)
					.build();
			leadWriteService.onholdLead(leadId, request);
			Map<String, Object> newValues = new HashMap<>(original);
			newValues.put(STATUS, ONHOLD);
			return ProcessingResult.success(original, newValues);
		} catch (Exception e) {
			return ProcessingResult.failed(ExceptionUtils.getRootCauseMessage(e), original);
		}
	}

	@Override
	protected Map<String, String> toRowDataMap(Map<String, Object> row) {
		Map<String, String> m = new HashMap<>();
		put(m, row, OnholdRowKeys.LEAD_IDENTIFIER);
		put(m, row, OnholdRowKeys.REASON_CODE);
		put(m, row, OnholdRowKeys.FOLLOW_UP_DATE);
		return m;
	}

	@Override
	protected String getRowReference(Map<String, Object> row) {
		UUID id = getUuid(row, OnholdRowKeys.LEAD_IDENTIFIER);
		return id != null ? id.toString() : null;
	}

	private static LocalDate getFollowUpDate(Map<String, Object> row) {
		Object v = row.get(OnholdRowKeys.FOLLOW_UP_DATE);
		if (v == null) return null;
		if (v instanceof LocalDate d) return d;
		String s = v.toString().trim();
		if (s.isEmpty()) return null;
		try {
			return LocalDate.parse(s);
		} catch (Exception e) {
			return null;
		}
	}

	private static void put(Map<String, String> target, Map<String, Object> source, String key) {
		Object v = source.get(key);
		if (v != null) target.put(key, v.toString());
	}

	private static String getString(Map<String, Object> row, String key) {
		Object v = row.get(key);
		if (v == null || "".equals(v)) return null;
		String s = v.toString().trim();
		return s.isEmpty() ? null : s;
	}

	private static UUID getUuid(Map<String, Object> row, String key) {
		String s = getString(row, key);
		if (s == null) return null;
		try {
			return UUID.fromString(s);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
