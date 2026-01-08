package com.nivasafinance.features.advisor.controller;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.advisor.dto.*;
import com.nivasafinance.features.advisor.service.AdvisorReadService;
import com.nivasafinance.features.advisor.service.AdvisorWriteService;
import jakarta.validation.Valid;
import com.nivasafinance.common.annotations.RequirePermission;
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
    @RequirePermission(permissionName = "READ_ADVISOR")
    public ResponseEntity<AdvisorTemplateResponse> getAdvisorTemplate() {
        AdvisorTemplateResponse response = advisorReadService.getAdvisorTemplate();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @RequirePermission(permissionName = "READ_ADVISOR")
    public ResponseEntity<PaginatedResponse<AdvisorBasicResponse>> getAllAdvisors(
            @Valid PaginationRequest paginationRequest,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String mobileNumber) {
        PaginatedResponse<AdvisorBasicResponse> response =
                advisorReadService.getAllAdvisors(paginationRequest, name, mobileNumber);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @RequirePermission(permissionName = "CREATE_ADVISOR")
    public ResponseEntity<IdentifierResponse> createAdvisor(@Valid @RequestBody CreateAdvisorRequest request) {
        UUID identifier = advisorWriteService.createAdvisor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new IdentifierResponse(identifier));
    }

    @GetMapping("/{identifier}")
    @RequirePermission(permissionName = "READ_ADVISOR")
    public ResponseEntity<AdvisorResponse> getAdvisorByIdentifier(@PathVariable UUID identifier) {
        AdvisorResponse response = advisorReadService.getAdvisorByIdentifier(identifier);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/search")
    @RequirePermission(permissionName = "READ_ADVISOR")
    public ResponseEntity<PaginatedResponse<AdvisorBasicResponse>> searchAdvisors(
            @Valid PaginationRequest paginationRequest,
            @Valid @RequestBody AdvisorSearchRequest searchRequest
    ) {
        PaginatedResponse<AdvisorBasicResponse> response =
                advisorReadService.searchAdvisors(paginationRequest, searchRequest);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{identifier}")
    @RequirePermission(permissionName = "UPDATE_ADVISOR")
    public ResponseEntity<Void> updateAdvisor(
            @PathVariable UUID identifier,
            @Valid @RequestBody UpdateAdvisorRequest request) {
        advisorWriteService.updateAdvisor(identifier, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk-assign-sales-owner")
    public ResponseEntity<BulkSalesOwnerAssignmentResponse> bulkAssignSalesOwner(
            @Valid @RequestBody BulkSalesOwnerAssignmentRequest request) {
        BulkSalesOwnerAssignmentResponse response = advisorWriteService.bulkAssignSalesOwner(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{identifier}/qualification-details")
    @RequirePermission(permissionName = "UPDATE_ADVISOR")
    public ResponseEntity<Void> updateQualificationDetails(
            @PathVariable UUID identifier,
            @Valid @RequestBody UpdateQualificationDetailsRequest request) {
        advisorWriteService.updateQualificationDetails(identifier, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{identifier}/occupation-details")
    @RequirePermission(permissionName = "UPDATE_ADVISOR")
    public ResponseEntity<Void> updateOccupationDetails(
            @PathVariable UUID identifier,
            @Valid @RequestBody UpdateOccupationDetailsRequest request) {
        advisorWriteService.updateOccupationDetails(identifier, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{identifier}/segmentation-details")
    @RequirePermission(permissionName = "UPDATE_ADVISOR")
    public ResponseEntity<Void> updateSegmentationDetails(
            @PathVariable UUID identifier,
            @Valid @RequestBody UpdateSegmentationDetailsRequest request) {
        advisorWriteService.updateSegmentationDetails(identifier, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{identifier}/reject")
    @RequirePermission(permissionName = "REJECT_ADVISOR")
    public ResponseEntity<Void> rejectAdvisor(
            @PathVariable UUID identifier,
            @Valid @RequestBody RejectAdvisorRequest request) {
        advisorWriteService.rejectAdvisor(identifier, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{identifier}/dormant")
    @RequirePermission(permissionName = "UPDATE_ADVISOR_STATUS")
    public ResponseEntity<Void> dormantAdvisor(
            @PathVariable UUID identifier,
            @Valid @RequestBody DormantAdvisorRequest request) {
        advisorWriteService.dormantAdvisor(identifier, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{identifier}/activate")
    @RequirePermission(permissionName = "UPDATE_ADVISOR_STATUS")
    public ResponseEntity<Void> activateAdvisor(
            @PathVariable UUID identifier) {
        advisorWriteService.activateAdvisor(identifier);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{identifier}/out-of-geo")
    public ResponseEntity<Void> outOfGeoAdvisor(
            @PathVariable UUID identifier,
            @Valid @RequestBody OutOfGeoAdvisorRequest request) {
        advisorWriteService.outOfGeoAdvisor(identifier, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/leads")
    @RequirePermission(permissionName = "READ_ADVISOR")
    public ResponseEntity<PaginatedResponse<AdvisorLeadResponse>> getLeadsByAdvisorId(
            @PathVariable("id") UUID advisorId,
            @Valid PaginationRequest paginationRequest) {
        PaginatedResponse<AdvisorLeadResponse> leads = advisorReadService.getLeadsByAdvisorId(
                advisorId, paginationRequest);
        return ResponseEntity.ok(leads);
    }

    @GetMapping("/dashboard")
    @RequirePermission(permissionName = "READ_ADVISOR")
    public ResponseEntity<PaginatedResponse<AdvisorDashboardResponse>> getAdvisorDashboard(
            @Valid PaginationRequest paginationRequest,
            @ModelAttribute AdvisorDashboardFilters filters) {
        PaginatedResponse<AdvisorDashboardResponse> response =
                advisorReadService.getAdvisorDashboard(paginationRequest, filters);
        return ResponseEntity.ok(response);
    }

}
