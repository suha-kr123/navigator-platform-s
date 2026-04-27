package com.nivasafinance.features.transaction.controller;

import com.nivasafinance.common.annotations.RequirePermission;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.transaction.dto.DeclineTransactionRequest;
import com.nivasafinance.features.transaction.dto.LeadTransactionResponse;
import com.nivasafinance.features.transaction.dto.FailTransactionRequest;
import com.nivasafinance.features.transaction.dto.PayTransactionRequest;
import com.nivasafinance.features.transaction.dto.TransactionDetailResponse;
import com.nivasafinance.features.transaction.dto.TransactionEventResponse;
import com.nivasafinance.features.transaction.service.TransactionReadService;
import com.nivasafinance.features.transaction.service.TransactionWriteService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
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
@RequestMapping(ApiConstants.V1 + "/transactions")
@AllArgsConstructor
public class TransactionController {

    private final TransactionReadService transactionReadService;
    private final TransactionWriteService transactionWriteService;

    @GetMapping("/referral/{referralCode}")
    @RequirePermission(permissionName = "READ_TRANSACTION")
    public ResponseEntity<List<LeadTransactionResponse>> getTransactionsByReferralCode(
            @PathVariable String referralCode) {
        List<LeadTransactionResponse> response = transactionReadService.getTransactionsByReferralCode(referralCode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{identifier}")
    @RequirePermission(permissionName = "READ_TRANSACTION")
    public ResponseEntity<TransactionDetailResponse> getTransactionDetail(
            @PathVariable UUID identifier) {
        TransactionDetailResponse response = transactionReadService.getByIdentifier(identifier);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{identifier}/events")
    @RequirePermission(permissionName = "READ_TRANSACTION")
    public ResponseEntity<List<TransactionEventResponse>> getTransactionEvents(
            @PathVariable UUID identifier) {
        List<TransactionEventResponse> response = transactionReadService.getEventHistory(identifier);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{identifier}/pay")
    @RequirePermission(permissionName = "PAY_TRANSACTION")
    public ResponseEntity<Void> payTransaction(
            @PathVariable UUID identifier,
            @Valid @RequestBody PayTransactionRequest request) {
        transactionWriteService.markPaid(identifier, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{identifier}/fail")
    @RequirePermission(permissionName = "PAY_TRANSACTION")
    public ResponseEntity<Void> failTransaction(
            @PathVariable UUID identifier,
            @Valid @RequestBody FailTransactionRequest request) {
        transactionWriteService.markFailed(identifier, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{identifier}/decline")
    @RequirePermission(permissionName = "DECLINE_TRANSACTION")
    public ResponseEntity<Void> declineTransaction(
            @PathVariable UUID identifier,
            @Valid @RequestBody DeclineTransactionRequest request) {
        transactionWriteService.declineTransaction(identifier, request);
        return ResponseEntity.noContent().build();
    }
}
