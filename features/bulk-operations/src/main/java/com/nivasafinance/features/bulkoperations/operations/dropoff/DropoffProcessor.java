package com.nivasafinance.features.bulkoperations.operations.dropoff;

import com.nivasafinance.common.exception.ExceptionUtils;
import com.nivasafinance.features.bulkoperations.common.config.BulkOperationCsvProperties;
import com.nivasafinance.features.bulkoperations.common.config.BulkOperationProcessingProperties;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationExceptionFactory;
import com.nivasafinance.features.bulkoperations.common.dto.ProcessingResult;
import com.nivasafinance.features.bulkoperations.common.entity.BulkOperation;
import com.nivasafinance.features.bulkoperations.engine.BaseBulkOperationProcessor;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationType;
import com.nivasafinance.features.bulkoperations.repository.BulkOperationRepository;
import com.nivasafinance.features.bulkoperations.service.BulkOperationProcessingPersistence;
import com.nivasafinance.features.bulkoperations.service.BulkOperationReportService;
import com.nivasafinance.features.bulkoperations.service.BulkOperationRowTransactionRunner;
import com.nivasafinance.features.bulkoperations.storage.BulkOperationFileStorageService;
import com.nivasafinance.features.lead.dto.DropoffLeadRequest;
import com.nivasafinance.features.lead.service.LeadWriteService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Processor for DROPOFF bulk operations. Calls LeadWriteService.dropoffLead for each row.
 * Dry-run skips actual domain calls and returns simulated success.
 */
@Component
public class DropoffProcessor extends BaseBulkOperationProcessor {

    private static final String STATUS = "status";
    private static final String DROPOFF = "DROPOFF";
    private final LeadWriteService leadWriteService;

    public DropoffProcessor(
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
        return BulkOperationType.DROPOFF;
    }

    @Override
    public ProcessingResult processRow(BulkOperation bulkOperation, Map<String, Object> row) {
        UUID leadId = getUuid(row, DropoffRowKeys.LEAD_IDENTIFIER);
        String reasonCode = getString(row, DropoffRowKeys.REASON_CODE);

        Map<String, Object> original = Map.of(
                DropoffRowKeys.LEAD_IDENTIFIER, leadId != null ? leadId.toString() : "",
                DropoffRowKeys.REASON_CODE, reasonCode != null ? reasonCode : "");

        if (leadId == null || reasonCode == null || reasonCode.isBlank()) {
            return ProcessingResult.failed(DropoffRowKeys.LEAD_IDENTIFIER + " and " + DropoffRowKeys.REASON_CODE + " are required", original);
        }

        if (bulkOperation.getStatus().isDryRunFlow()) {
            Map<String, Object> newValues = new HashMap<>(original);
            newValues.put(STATUS, DROPOFF + " (dry-run)");
            return ProcessingResult.success(original, newValues);
        }

        try {
            DropoffLeadRequest request = DropoffLeadRequest.builder()
                    .reasonCode(reasonCode)
                    .build();
            leadWriteService.dropoffLead(leadId, request);
            Map<String, Object> newValues = new HashMap<>(original);
            newValues.put(STATUS, DROPOFF);
            return ProcessingResult.success(original, newValues);
        } catch (Exception e) {
            return ProcessingResult.failed(ExceptionUtils.getRootCauseMessage(e), original);
        }
    }

    @Override
    protected Map<String, String> toRowDataMap(Map<String, Object> row) {
        Map<String, String> m = new HashMap<>();
        put(m, row, DropoffRowKeys.LEAD_IDENTIFIER);
        put(m, row, DropoffRowKeys.REASON_CODE);
        return m;
    }

    @Override
    protected String getRowReference(Map<String, Object> row) {
        UUID id = getUuid(row, DropoffRowKeys.LEAD_IDENTIFIER);
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
