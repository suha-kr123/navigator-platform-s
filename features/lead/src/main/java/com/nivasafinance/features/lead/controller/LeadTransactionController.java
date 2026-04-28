package com.nivasafinance.features.lead.controller;

import com.nivasafinance.common.annotations.RequirePermission;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.lead.service.LeadTransactionReadService;
import com.nivasafinance.features.lead.service.LeadTransactionWriteService;
import com.nivasafinance.features.transaction.dto.CreateLeadTransactionRequest;
import com.nivasafinance.features.transaction.dto.CreateLeadTransactionResponse;
import com.nivasafinance.features.transaction.dto.LeadTransactionResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.V1 + "/lead")
@AllArgsConstructor
public class LeadTransactionController {

    private final LeadTransactionReadService leadTransactionReadService;
    private final LeadTransactionWriteService leadTransactionWriteService;

    @GetMapping("/{leadIdentifier}/lead-transactions")
    @RequirePermission(permissionName = "READ_TRANSACTION")
    public ResponseEntity<List<LeadTransactionResponse>> getLeadTransactions(
            @PathVariable UUID leadIdentifier) {
        List<LeadTransactionResponse> response = leadTransactionReadService.getLeadTransactions(leadIdentifier);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{leadIdentifier}/lead-transactions")
    @RequirePermission(permissionName = "CREATE_TRANSACTION")
    public ResponseEntity<CreateLeadTransactionResponse> createLeadTransaction(
            @PathVariable UUID leadIdentifier,
            @Valid @RequestBody CreateLeadTransactionRequest request) {
        CreateLeadTransactionResponse response = leadTransactionWriteService.createLeadTransaction(leadIdentifier, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
