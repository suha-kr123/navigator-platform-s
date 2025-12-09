package com.nivasafinance.features.lead.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lead.dto.*;
import com.nivasafinance.features.lead.dto.BulkSalesOwnerAssignmentRequest;
import com.nivasafinance.features.lead.dto.BulkSalesOwnerAssignmentResponse;
import com.nivasafinance.features.lead.dto.UpdateCallDetailsRequest;
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

    @GetMapping("/dashboard")
    public ResponseEntity<PaginatedResponse<LeadDashboardResponse>> getLeadDashboard(
            @Valid PaginationRequest paginationRequest,
            @ModelAttribute LeadDashboardFilters filters
    ) {
        PaginatedResponse<LeadDashboardResponse> response =
                leadReadService.getLeadDashboard(paginationRequest, filters);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard/filters")
    public ResponseEntity<LeadDashboardFiltersResponse> getLeadDashboardFilters(
            @ModelAttribute LeadDashboardFiltersFilters filters
    ) {
        LeadDashboardFiltersResponse response = leadReadService.getLeadDashboardFilters(filters);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/template")
    public ResponseEntity<LeadTemplateResponse> getLeadTemplate() {
        LeadTemplateResponse response = leadReadService.getLeadTemplate();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CreateLeadResponse> createLead(@Valid @RequestBody CreateLeadRequest request) {
        CreateLeadResponse response = leadWriteService.createLead(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{leadId}")
    public ResponseEntity<LeadResponse> getLeads(@PathVariable UUID leadId) {
        return ResponseEntity.status(HttpStatus.OK).body(leadReadService.getLeadByIdentifier(leadId));
    }

    @PutMapping("/{leadId}")
    public ResponseEntity<Void> updateLead(
            @PathVariable UUID leadId,
            @Valid @RequestBody UpdateLeadRequest request) {
        leadWriteService.updateLead(leadId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk-assign-sales-owner")
    public ResponseEntity<BulkSalesOwnerAssignmentResponse> bulkAssignSalesOwner(
            @Valid @RequestBody BulkSalesOwnerAssignmentRequest request) {
        BulkSalesOwnerAssignmentResponse response = leadWriteService.bulkAssignSalesOwner(request);
        return ResponseEntity.ok(response);
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

    @PutMapping("/{leadId}/credit-details")
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

    @PutMapping("/{leadId}/proposed-details")
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

    @PutMapping("/{leadId}/property-details")
    public ResponseEntity<Void> updatePropertyDetails(
            @PathVariable UUID leadId,
            @Valid @RequestBody UpdatePropertyDetailsRequest request) {
        leadWriteService.updatePropertyDetails(leadId, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{leadId}/property-details")
    public ResponseEntity<PropertyDetailsResponse> getPropertyDetails(@PathVariable UUID leadId) {
        PropertyDetailsResponse response = leadReadService.getPropertyDetails(leadId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{leadId}/sourcing-details")
    public ResponseEntity<Void> updateSourcingDetails(
            @PathVariable UUID leadId,
            @Valid @RequestBody UpdateSourcingDetailsRequest request) {
        leadWriteService.updateSourcingDetails(leadId, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{leadId}/sourcing-details")
    public ResponseEntity<SourcingDetailsResponse> getSourcingDetails(@PathVariable UUID leadId) {
        SourcingDetailsResponse response = leadReadService.getSourcingDetails(leadId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{leadId}/disbursement-details")
    public ResponseEntity<Void> updateDisbursementDetails(
            @PathVariable UUID leadId,
            @Valid @RequestBody UpdateDisbursementDetailsRequest request) {
        leadWriteService.updateDisbursementDetails(leadId, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{leadId}/disbursement-details")
    public ResponseEntity<DisbursementDetailsResponse> getDisbursementDetails(@PathVariable UUID leadId) {
        DisbursementDetailsResponse response = leadReadService.getDisbursementDetails(leadId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{leadId}/disbursement-details/tranches")
    public ResponseEntity<Void> createTranche(
            @PathVariable UUID leadId,
            @Valid @RequestBody CreateTrancheRequest request) {
        leadWriteService.createTranche(leadId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{leadId}/disbursement-details/tranches/{trancheIdentifier}")
    public ResponseEntity<Void> updateTranche(
            @PathVariable UUID leadId,
            @PathVariable UUID trancheIdentifier,
            @Valid @RequestBody UpdateTrancheRequest request) {
        leadWriteService.updateTranche(leadId, trancheIdentifier, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{leadId}/disbursement-details/tranches/{trancheIdentifier}")
    public ResponseEntity<TrancheResponse> getTrancheByIdentifier(
            @PathVariable UUID leadId,
            @PathVariable UUID trancheIdentifier) {
        TrancheResponse response = leadReadService.getTrancheByIdentifier(leadId, trancheIdentifier);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{leadId}/disbursement-details/tranches/{trancheIdentifier}")
    public ResponseEntity<Void> deleteTranche(
            @PathVariable UUID leadId,
            @PathVariable UUID trancheIdentifier) {
        leadWriteService.deleteTranche(leadId, trancheIdentifier);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{leadId}/status/reject")
    public ResponseEntity<Void> rejectLead(
            @PathVariable UUID leadId,
            @Valid @RequestBody RejectLeadRequest request) {
        leadWriteService.rejectLead(leadId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{leadId}/status/undo-reject")
    public ResponseEntity<Void> undoRejectLead(
            @PathVariable UUID leadId) {
        leadWriteService.undoRejectLead(leadId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{leadId}/status/withdraw")
    public ResponseEntity<Void> withdrawLead(
            @PathVariable UUID leadId,
            @Valid @RequestBody WithdrawLeadRequest request) {
        leadWriteService.withdrawLead(leadId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{leadId}/status/complete")
    public ResponseEntity<Void> completeLead(@PathVariable UUID leadId) {
        leadWriteService.completeLead(leadId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{leadId}/status/onhold")
    public ResponseEntity<Void> onholdLead(
            @PathVariable UUID leadId,
            @Valid @RequestBody OnholdLeadRequest request) {
        leadWriteService.onholdLead(leadId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{leadId}/status/resume")
    public ResponseEntity<Void> resumeLead(@PathVariable UUID leadId) {
        leadWriteService.resumeLead(leadId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{leadId}/status/dropoff")
    public ResponseEntity<Void> dropoffLead(
            @PathVariable UUID leadId,
            @Valid @RequestBody DropoffLeadRequest request) {
        leadWriteService.dropoffLead(leadId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/search")
    public ResponseEntity<PaginatedResponse<LeadSearchResponse>> searchLeads(
            @Valid PaginationRequest paginationRequest,
            @Valid @RequestBody LeadSearchRequest searchRequest
    ) {
        PaginatedResponse<LeadSearchResponse> response =
                leadReadService.searchLeads(paginationRequest, searchRequest);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{leadId}/call-details")
    public ResponseEntity<Void> updateCallDetails(
            @PathVariable UUID leadId,
            @Valid @RequestBody UpdateCallDetailsRequest request) {
        leadWriteService.updateCallDetails(leadId, request);
        return ResponseEntity.noContent().build();
    }
}
