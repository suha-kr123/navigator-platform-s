package com.nivasafinance.features.bulkoperations.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.bulkoperations.common.dto.BulkOperationResponse;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationType;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface BulkOperationService {

    BulkOperationResponse uploadCsv(MultipartFile file, BulkOperationType operationType, boolean dryRun);

    BulkOperationResponse getOperationStatus(UUID operationId);

    void cancelOperation(UUID operationId);

    String getSummaryReportCsv(UUID operationId);

    PaginatedResponse<BulkOperationResponse> getOperationHistory(PaginationRequest paginationRequest);

    BulkOperationResponse executeDryRunOperation(UUID dryRunOperationId);
}
