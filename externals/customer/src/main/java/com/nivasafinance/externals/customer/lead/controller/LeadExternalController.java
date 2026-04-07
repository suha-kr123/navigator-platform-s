package com.nivasafinance.externals.customer.lead.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.externals.customer.lead.service.LeadExternalService;
import com.nivasafinance.features.lead.dto.CurrentCustomerFormStepResponse;
import com.nivasafinance.features.lead.dto.LeadBREResultExecuteResponse;
import com.nivasafinance.features.lead.dto.LeadContactResponse;
import com.nivasafinance.features.lead.dto.LeadEligibilityResponse;
import com.nivasafinance.features.lead.dto.LeadResponse;
import com.nivasafinance.features.lead.dto.PatchLeadRequest;
import com.nivasafinance.features.lead.dto.DocumentChecklistResponse;
import com.nivasafinance.features.lead.dto.IncomeObligationDetailsResponse;
import com.nivasafinance.features.lead.dto.PreliminaryDetailsResponse;
import com.nivasafinance.features.lead.dto.PropertyDetailsResponse;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.OPEN_API_V1 + "/customer/leads")
@RequiredArgsConstructor
@Slf4j
public class LeadExternalController {

    private final LeadExternalService leadExternalService;

    @PatchMapping("/{leadIdentifier}")
    public ResponseEntity<Void> patchLead(
            @PathVariable UUID leadIdentifier,
            @RequestBody PatchLeadRequest request) {
        leadExternalService.patchLead(leadIdentifier, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{leadIdentifier}")
    public ResponseEntity<LeadResponse> getLead(@PathVariable UUID leadIdentifier) {
        LeadResponse response = leadExternalService.getLeadByIdentifier(leadIdentifier);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{leadIdentifier}/current-form-step")
    public ResponseEntity<CurrentCustomerFormStepResponse> getCurrentCustomerFormStep(
            @PathVariable UUID leadIdentifier) {
        CurrentCustomerFormStepResponse response = leadExternalService.getCurrentCustomerFormStep(leadIdentifier);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{leadIdentifier}/contacts")
    public ResponseEntity<List<LeadContactResponse>> getContacts(@PathVariable UUID leadIdentifier) {
        List<LeadContactResponse> contacts = leadExternalService.getContacts(leadIdentifier);
        return ResponseEntity.ok(contacts);
    }

    @GetMapping("/{leadIdentifier}/property-details")
    public ResponseEntity<PropertyDetailsResponse> getPropertyDetails(@PathVariable UUID leadIdentifier) {
        PropertyDetailsResponse response = leadExternalService.getPropertyDetails(leadIdentifier);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{leadIdentifier}/income-obligation-details")
    public ResponseEntity<IncomeObligationDetailsResponse> getIncomeObligationDetails(
            @PathVariable UUID leadIdentifier) {
        IncomeObligationDetailsResponse response = leadExternalService.getIncomeObligationDetails(leadIdentifier);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{leadIdentifier}/document-checklists")
    public ResponseEntity<DocumentChecklistResponse> getDocumentChecklist(@PathVariable UUID leadIdentifier) {
        DocumentChecklistResponse response = leadExternalService.getDocumentChecklist(leadIdentifier);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{leadIdentifier}/preliminary-details")
    public ResponseEntity<PreliminaryDetailsResponse> getPreliminaryDetails(@PathVariable UUID leadIdentifier) {
        PreliminaryDetailsResponse response = leadExternalService.getPreliminaryDetails(leadIdentifier);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{leadIdentifier}/bre/execute/eligibility")
    public ResponseEntity<LeadBREResultExecuteResponse> executeEligibility(@PathVariable UUID leadIdentifier) {
        LeadBREResultExecuteResponse response = leadExternalService.executeEligibility(leadIdentifier);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{leadIdentifier}/bre/execute/eligibility/latest")
    public ResponseEntity<LeadEligibilityResponse> getLatestEligibility(@PathVariable UUID leadIdentifier) {
        return leadExternalService.getLatestEligibility(leadIdentifier)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
