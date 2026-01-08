package com.nivasafinance.features.leadstages.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.common.annotations.RequirePermission;
import com.nivasafinance.features.leadstages.dto.ChangeAssignmentRequest;
import com.nivasafinance.features.leadstages.dto.ChangeSubStageRequest;
import com.nivasafinance.features.leadstages.dto.LeadStageHistoryResponse;
import com.nivasafinance.features.leadstages.dto.StageTransitionRequest;
import com.nivasafinance.features.leadstages.service.LeadStageHistoryReadService;
import com.nivasafinance.features.leadstages.service.LeadStageHistoryWriteService;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.V1 + "/leads/{leadId}/stages")
@AllArgsConstructor
public class LeadStageController {

    private final LeadStageHistoryWriteService leadStageHistoryWriteService;
    private final LeadStageHistoryReadService leadStageHistoryReadService;

    @PostMapping("/transition")
    @RequirePermission(permissionName = "CREATE_LEAD_STAGES")
    public ResponseEntity<LeadStageHistoryResponse> transitionStage(
            @PathVariable UUID leadId,
            @Valid @RequestBody StageTransitionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(LeadStageHistoryResponse
                        .from(leadStageHistoryWriteService.createStageEntry(leadId, request)));
    }

    @GetMapping("/history")
    @RequirePermission(permissionName = "READ_LEAD_STAGES")
    public ResponseEntity<PaginatedResponse<LeadStageHistoryResponse>> getStageHistory(
            @PathVariable UUID leadId,
            @Valid PaginationRequest paginationRequest) {
        return ResponseEntity
                .ok(leadStageHistoryReadService.getStageHistoryByLeadId(leadId, paginationRequest));
    }

    @PutMapping("/assignment")
    @RequirePermission(permissionName = "UPDATE_LEAD_STAGES")
    public ResponseEntity<LeadStageHistoryResponse> changeAssignment(
            @PathVariable UUID leadId,
            @Valid @RequestBody ChangeAssignmentRequest request) {
        return ResponseEntity.ok(LeadStageHistoryResponse.from(
                leadStageHistoryWriteService.changeAssignment(leadId, request.getStageKey(),
                        request.getNewAssignedTo())));
    }

    @PutMapping("/sub-stage")
    @RequirePermission(permissionName = "UPDATE_LEAD_STAGES")
    public ResponseEntity<LeadStageHistoryResponse> changeSubStage(
            @PathVariable UUID leadId,
            @Valid @RequestBody ChangeSubStageRequest request) {
        return ResponseEntity.ok(LeadStageHistoryResponse.from(
                leadStageHistoryWriteService.changeSubStage(leadId, request.getStageKey(),
                        request.getSubStageKey())));
    }
}
