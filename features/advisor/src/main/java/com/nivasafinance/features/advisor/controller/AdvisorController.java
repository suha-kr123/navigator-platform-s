package com.nivasafinance.features.advisor.controller;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.advisor.dto.*;
import com.nivasafinance.features.advisor.service.AdvisorReadService;
import com.nivasafinance.features.advisor.service.AdvisorWriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping(ApiConstants.V1 + "/advisors")
@RequiredArgsConstructor
public class AdvisorController {

    private final AdvisorWriteService advisorWriteService;
    private final AdvisorReadService advisorReadService;

    @GetMapping("/template")
    public ResponseEntity<AdvisorTemplateResponse> getAdvisorTemplate() {
        AdvisorTemplateResponse response = advisorReadService.getAdvisorTemplate();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<IdentifierResponse> createAdvisor(@Valid @RequestBody CreateAdvisorRequest request) {
        UUID identifier = advisorWriteService.createAdvisor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new IdentifierResponse(identifier));
    }

    @GetMapping("/{identifier}")
    public ResponseEntity<AdvisorResponse> getAdvisorByIdentifier(@PathVariable UUID identifier) {
        AdvisorResponse response = advisorReadService.getAdvisorByIdentifier(identifier);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/search")
    public ResponseEntity<PaginatedResponse<AdvisorSearchResponse>> searchAdvisors(
            @Valid PaginationRequest paginationRequest,
            @Valid @RequestBody AdvisorSearchRequest searchRequest
    ) {
        PaginatedResponse<AdvisorSearchResponse> response =
                advisorReadService.searchAdvisors(paginationRequest, searchRequest);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{identifier}")
    public ResponseEntity<Void> updateAdvisor(
            @PathVariable UUID identifier,
            @Valid @RequestBody UpdateAdvisorRequest request) {
        advisorWriteService.updateAdvisor(identifier, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{identifier}/qualification-details")
    public ResponseEntity<Void> updateQualificationDetails(
            @PathVariable UUID identifier,
            @Valid @RequestBody UpdateQualificationDetailsRequest request) {
        advisorWriteService.updateQualificationDetails(identifier, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{identifier}/occupation-details")
    public ResponseEntity<Void> updateOccupationDetails(
            @PathVariable UUID identifier,
            @Valid @RequestBody UpdateOccupationDetailsRequest request) {
        advisorWriteService.updateOccupationDetails(identifier, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{identifier}/segmentation-details")
    public ResponseEntity<Void> updateSegmentationDetails(
            @PathVariable UUID identifier,
            @Valid @RequestBody UpdateSegmentationDetailsRequest request) {
        advisorWriteService.updateSegmentationDetails(identifier, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{identifier}/reject")
    public ResponseEntity<Void> rejectAdvisor(
            @PathVariable UUID identifier,
            @Valid @RequestBody RejectAdvisorRequest request) {
        advisorWriteService.rejectAdvisor(identifier, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{identifier}/dormant")
    public ResponseEntity<Void> dormantAdvisor(
            @PathVariable UUID identifier,
            @Valid @RequestBody DormantAdvisorRequest request) {
        advisorWriteService.dormantAdvisor(identifier, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{identifier}/activate")
    public ResponseEntity<Void> activateAdvisor(
            @PathVariable UUID identifier) {
        advisorWriteService.activateAdvisor(identifier);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/leads")
    public ResponseEntity<PaginatedResponse<AdvisorLeadResponse>> getLeadsByAdvisorId(
            @PathVariable("id") UUID advisorId,
            @Valid PaginationRequest paginationRequest) {
        PaginatedResponse<AdvisorLeadResponse> leads = advisorReadService.getLeadsByAdvisorId(
                advisorId, paginationRequest);
        return ResponseEntity.ok(leads);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<PaginatedResponse<AdvisorDashboardResponse>> getAdvisorDashboard(
            @Valid PaginationRequest paginationRequest,
            @ModelAttribute AdvisorDashboardFilters filters) {
        PaginatedResponse<AdvisorDashboardResponse> response =
                advisorReadService.getAdvisorDashboard(paginationRequest, filters);
        return ResponseEntity.ok(response);
    }

}
