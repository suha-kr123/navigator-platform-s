package com.nivasafinance.features.lead.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.lead.dto.CreateLeadRequest;
import com.nivasafinance.features.lead.dto.CreateLeadResponse;
import com.nivasafinance.features.lead.dto.CreditDetailsResponse;
import com.nivasafinance.features.lead.dto.LeadResponse;
import com.nivasafinance.features.lead.dto.PreliminaryDetailsResponse;
import com.nivasafinance.features.lead.dto.ProposedDetailsResponse;
import com.nivasafinance.features.lead.dto.UpdateCreditDetailsRequest;
import com.nivasafinance.features.lead.dto.UpdatePreliminaryDetailsRequest;
import com.nivasafinance.features.lead.dto.UpdateProposedDetailsRequest;
import com.nivasafinance.features.lead.service.LeadReadService;
import com.nivasafinance.features.lead.service.LeadWriteService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.V1 + "/leads")
@AllArgsConstructor
public class LeadController {

    private final LeadWriteService leadWriteService;
    private final LeadReadService leadReadService;

    @PostMapping
    public ResponseEntity<CreateLeadResponse> createLead(@Valid @RequestBody CreateLeadRequest request) {
        CreateLeadResponse response = leadWriteService.createLead(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{leadId}")
    public ResponseEntity<LeadResponse> getLeads(@PathVariable UUID leadId) {
        return ResponseEntity.status(HttpStatus.OK).body(leadReadService.getLeadByIdentifier(leadId));
    }

    @PutMapping("/{leadId}/preliminary-details")
    public ResponseEntity<Void> updatePreliminaryDetails(
            @PathVariable UUID leadId,
            @RequestBody UpdatePreliminaryDetailsRequest request) {
        leadWriteService.updatePreliminaryDetails(leadId, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{leadId}/preliminary-details")
    public ResponseEntity<PreliminaryDetailsResponse> getPreliminaryDetails(@PathVariable UUID leadId) {
        PreliminaryDetailsResponse response = leadReadService.getPreliminaryDetails(leadId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{leadId}/credit-details")
    public ResponseEntity<Void> updateCreditDetails(
            @PathVariable UUID leadId,
            @RequestBody UpdateCreditDetailsRequest request) {
        leadWriteService.updateCreditDetails(leadId, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{leadId}/credit-details")
    public ResponseEntity<CreditDetailsResponse> getCreditDetails(@PathVariable UUID leadId) {
        CreditDetailsResponse response = leadReadService.getCreditDetails(leadId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{leadId}/proposed-details")
    public ResponseEntity<Void> updateProposedDetails(
            @PathVariable UUID leadId,
            @Valid @RequestBody UpdateProposedDetailsRequest request) {
        leadWriteService.updateProposedDetails(leadId, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{leadId}/proposed-details")
    public ResponseEntity<ProposedDetailsResponse> getProposedDetails(@PathVariable UUID leadId) {
        ProposedDetailsResponse response = leadReadService.getProposedDetails(leadId);
        return ResponseEntity.ok(response);
    }
}
