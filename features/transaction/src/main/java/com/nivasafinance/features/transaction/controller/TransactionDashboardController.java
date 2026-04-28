package com.nivasafinance.features.transaction.controller;

import com.nivasafinance.common.annotations.RequirePermission;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.transaction.dto.DisbursedLeadResponse;
import com.nivasafinance.features.transaction.dto.LeadTransactionResponse;
import com.nivasafinance.features.transaction.dto.TransactionDashboardFilters;
import com.nivasafinance.features.transaction.dto.TransactionSearchRequest;
import com.nivasafinance.features.transaction.dto.TransactionSearchResponse;
import com.nivasafinance.features.transaction.service.TransactionReadService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.V1 + "/transaction-dashboard")
@AllArgsConstructor
public class TransactionDashboardController {

    private final TransactionReadService transactionReadService;

    @GetMapping("/pending")
    @RequirePermission(permissionName = "READ_TRANSACTION")
    public ResponseEntity<PaginatedResponse<DisbursedLeadResponse>> getDisbursedLeadsPendingTransaction(
            @Valid PaginationRequest paginationRequest) {
        PaginatedResponse<DisbursedLeadResponse> response =
                transactionReadService.getDisbursedLeadsPendingTransaction(paginationRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @RequirePermission(permissionName = "READ_TRANSACTION")
    public ResponseEntity<PaginatedResponse<LeadTransactionResponse>> getTransactionDashboard(
            @Valid PaginationRequest paginationRequest,
            @ModelAttribute TransactionDashboardFilters filters) {
        PaginatedResponse<LeadTransactionResponse> response =
                transactionReadService.getTransactionDashboard(paginationRequest, filters);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/search/lead")
    @RequirePermission(permissionName = "READ_TRANSACTION")
    public ResponseEntity<TransactionSearchResponse> searchByLeadMobile(
            @Valid @RequestBody TransactionSearchRequest request) {
        TransactionSearchResponse response = transactionReadService.searchByLeadMobileNumber(request.getMobileNumber());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/search/advisor")
    @RequirePermission(permissionName = "READ_TRANSACTION")
    public ResponseEntity<TransactionSearchResponse> searchByAdvisorMobile(
            @Valid @RequestBody TransactionSearchRequest request) {
        TransactionSearchResponse response = transactionReadService.searchByAdvisorMobileNumber(request.getMobileNumber());
        return ResponseEntity.ok(response);
    }
}
