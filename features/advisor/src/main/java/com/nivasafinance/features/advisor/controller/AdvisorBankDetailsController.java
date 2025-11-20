package com.nivasafinance.features.advisor.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.advisor.dto.*;
import com.nivasafinance.features.advisor.dto.BankIdentifierResponse;
import com.nivasafinance.features.advisor.service.AdvisorBankDetailsReadService;
import com.nivasafinance.features.advisor.service.AdvisorBankDetailsWriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.V1 + "/advisors")
@RequiredArgsConstructor
public class AdvisorBankDetailsController {

    private final AdvisorBankDetailsWriteService advisorBankDetailsWriteService;
    private final AdvisorBankDetailsReadService advisorBankDetailsReadService;

    @PostMapping("/{identifier}/bank-details")
    public ResponseEntity<BankIdentifierResponse> addBankDetails(
            @PathVariable UUID identifier,
            @Valid @RequestBody AddBankDetailsRequest request) {
        UUID bankIdentifier = advisorBankDetailsWriteService.addBankDetails(identifier, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new BankIdentifierResponse(bankIdentifier));
    }

    @GetMapping("/{identifier}/bank-details")
    public ResponseEntity<List<BankDetailsResponse>> getAllBankDetails(@PathVariable UUID identifier) {
        List<BankDetailsResponse> responses = advisorBankDetailsReadService.getAllBankDetails(identifier);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{identifier}/bank-details/{bankIdentifier}")
    public ResponseEntity<Void> updateBankDetails(
            @PathVariable UUID identifier,
            @PathVariable UUID bankIdentifier,
            @Valid @RequestBody UpdateBankDetailsRequest request) {
        advisorBankDetailsWriteService.updateBankDetails(identifier, bankIdentifier, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{identifier}/bank-details/{bankIdentifier}/activate")
    public ResponseEntity<BankIdentifierResponse> activateBankDetails(
            @PathVariable UUID identifier,
            @PathVariable UUID bankIdentifier) {
        UUID resultBankIdentifier = advisorBankDetailsWriteService.activateBankDetails(identifier, bankIdentifier);
        return ResponseEntity.ok(new BankIdentifierResponse(resultBankIdentifier));
    }

    @PostMapping("/{identifier}/bank-details/{bankIdentifier}/deactivate")
    public ResponseEntity<BankIdentifierResponse> deactivateBankDetails(
            @PathVariable UUID identifier,
            @PathVariable UUID bankIdentifier) {
        UUID resultBankIdentifier = advisorBankDetailsWriteService.deactivateBankDetails(identifier, bankIdentifier);
        return ResponseEntity.ok(new BankIdentifierResponse(resultBankIdentifier));
    }
}

