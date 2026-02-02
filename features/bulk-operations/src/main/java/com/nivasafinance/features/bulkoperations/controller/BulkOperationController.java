package com.nivasafinance.features.bulkoperations.controller;

import com.nivasafinance.common.annotations.RequirePermission;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.bulkoperations.common.dto.BulkOperationResponse;
import com.nivasafinance.features.bulkoperations.common.dto.BulkOperationTypeResponse;
import com.nivasafinance.features.bulkoperations.engine.BulkOperationType;
import com.nivasafinance.features.bulkoperations.service.BulkOperationService;
import com.nivasafinance.features.bulkoperations.service.BulkOperationTypeConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Retryable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bulk-operations")
@RequiredArgsConstructor
@Validated
public class BulkOperationController {

    private final BulkOperationService bulkOperationService;
    private final BulkOperationTypeConfigService operationTypeConfigService;

    @GetMapping("/operation-types")
    @RequirePermission(permissionName = "VIEW_BULK_OPERATION_TYPES")
    public List<BulkOperationTypeResponse> getAllOperationTypes() {
        return operationTypeConfigService.getAllOperationTypes();
    }
    
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    @RequirePermission(permissionName = "BULK_UPDATE_LEAD")
    public BulkOperationResponse uploadCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam("operationType") BulkOperationType operationType,
            @RequestParam(defaultValue = "false") boolean dryRun) {
        return bulkOperationService.uploadCsv(file, operationType, dryRun);
    }

    @GetMapping("/operations/{operationId}")
    @RequirePermission(permissionName = "VIEW_BULK_OPERATION_STATUS")
    @Retryable(maxAttempts = 3)
    public BulkOperationResponse getOperationStatus(@PathVariable UUID operationId) {
        return bulkOperationService.getOperationStatus(operationId);
    }
    
    @PostMapping("/operations/{operationId}/cancel")
    @RequirePermission(permissionName = "CANCEL_BULK_OPERATION")
    public ResponseEntity<Void> cancelOperation(@PathVariable UUID operationId) {
        bulkOperationService.cancelOperation(operationId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/operations/{operationId}/execute-dry-run")
    @RequirePermission(permissionName = "PREVIEW_BULK_OPERATION")
    public BulkOperationResponse executeDryRun(@PathVariable UUID operationId) {
        return bulkOperationService.executeDryRunOperation(operationId);
    }

    @GetMapping("/operations")
    @RequirePermission(permissionName = "VIEW_BULK_OPERATION_STATUS")
    public PaginatedResponse<BulkOperationResponse> getOperationHistory(
            @Valid PaginationRequest paginationRequest) {
        return bulkOperationService.getOperationHistory(paginationRequest);
    }

    @GetMapping(value = "/operations/{operationId}/summary", produces = "text/csv")
    @RequirePermission(permissionName = "DOWNLOAD_BULK_OPERATION_REPORTS")
    public ResponseEntity<String> getSummaryReportCsv(@PathVariable UUID operationId) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"summary_" + operationId + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bulkOperationService.getSummaryReportCsv(operationId));
    }
}
    