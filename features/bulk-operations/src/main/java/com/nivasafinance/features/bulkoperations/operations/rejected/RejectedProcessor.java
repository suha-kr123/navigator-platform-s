package com.nivasafinance.features.bulkoperations.operations.rejected;

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
import com.nivasafinance.features.lead.dto.RejectLeadRequest;
import com.nivasafinance.features.lead.service.LeadWriteService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Processor for REJECTED bulk operations. Calls LeadWriteService.rejectLead for each row.
 * Dry-run skips actual domain calls and returns simulated success.
 */
@Component
public class RejectedProcessor extends BaseBulkOperationProcessor {

	private static final String STATUS = "status";
	private static final String REJECTED = "REJECTED";

	private final LeadWriteService leadWriteService;

	public RejectedProcessor(
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
		return BulkOperationType.REJECTED;
	}

	@Override
	public ProcessingResult processRow(BulkOperation bulkOperation, Map<String, Object> row) {
		UUID leadId = getUuid(row, RejectedRowKeys.LEAD_IDENTIFIER);
		String reasonCode = getString(row, RejectedRowKeys.REASON_CODE);

		Map<String, Object> original = Map.of(
				RejectedRowKeys.LEAD_IDENTIFIER, leadId != null ? leadId.toString() : "",
				RejectedRowKeys.REASON_CODE, reasonCode != null ? reasonCode : "");

		if (leadId == null || reasonCode == null || reasonCode.isBlank()) {
			return ProcessingResult.failed(RejectedRowKeys.LEAD_IDENTIFIER + " and " + RejectedRowKeys.REASON_CODE + " are required", original);
		}

		if (bulkOperation.getStatus().isDryRunFlow()) {
			Map<String, Object> newValues = new HashMap<>(original);
			newValues.put(STATUS, REJECTED + " (dry-run)");
			return ProcessingResult.success(original, newValues);
		}

		try {
			RejectLeadRequest request = RejectLeadRequest.builder()
					.reasonCode(reasonCode)
					.build();
			leadWriteService.rejectLead(leadId, request);
			Map<String, Object> newValues = new HashMap<>(original);
			newValues.put(STATUS, REJECTED);
			return ProcessingResult.success(original, newValues);
		} catch (Exception e) {
			return ProcessingResult.failed(ExceptionUtils.getRootCauseMessage(e), original);
		}
	}

	@Override
	protected Map<String, String> toRowDataMap(Map<String, Object> row) {
		Map<String, String> m = new HashMap<>();
		put(m, row, RejectedRowKeys.LEAD_IDENTIFIER);
		put(m, row, RejectedRowKeys.REASON_CODE);
		return m;
	}

	@Override
	protected String getRowReference(Map<String, Object> row) {
		UUID id = getUuid(row, RejectedRowKeys.LEAD_IDENTIFIER);
		return id != null ? id.toString() : null;
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
