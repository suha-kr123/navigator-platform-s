package com.nivasafinance.features.lead.controller;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.common.annotations.RequirePermission;
import com.nivasafinance.features.lead.dto.CreateExternalCallLogRequest;
import com.nivasafinance.features.lead.dto.CreateExternalCallLogResponse;
import com.nivasafinance.features.lead.dto.CreateLeadCallRequest;
import com.nivasafinance.features.lead.dto.CreateLeadCallResponse;
import com.nivasafinance.features.lead.dto.LeadCallLogResponse;
import com.nivasafinance.features.lead.dto.LeadCallSummaryResponse;
import com.nivasafinance.features.lead.dto.LeadUpdateCallLog;
import com.nivasafinance.features.lead.service.LeadCallReadService;
import com.nivasafinance.features.lead.service.LeadCallWriteService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.V1 + "/leads/{leadId}/call")
@AllArgsConstructor
public class LeadCallController {

    private final LeadCallWriteService leadCallWriteService;
    private final LeadCallReadService leadCallReadService;

    @PostMapping
    @RequirePermission(permissionName = "CREATE_LEAD_CALL")
    public ResponseEntity<CreateLeadCallResponse> callLeadContact(
            @PathVariable UUID leadId,
            @Valid @RequestBody CreateLeadCallRequest request
    ) {
        CreateLeadCallResponse response = leadCallWriteService.callContact(leadId, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/summary")
    @RequirePermission(permissionName = "READ_LEAD_CALL")
    public ResponseEntity<LeadCallSummaryResponse> getCallSummary(@PathVariable UUID leadId) {
        return ResponseEntity.ok(leadCallReadService.getCallSummary(leadId));
    }

    @GetMapping("/logs")
    @RequirePermission(permissionName = "READ_LEAD_CALL")
    public ResponseEntity<PaginatedResponse<LeadCallLogResponse>> getCallLogs(
            @PathVariable UUID leadId,
            @Valid PaginationRequest paginationRequest
    ) {
        PaginatedResponse<LeadCallLogResponse> response = leadCallReadService.getCallLogs(leadId, paginationRequest);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/external/{externalId}")
    @RequirePermission(permissionName = "UPDATE_LEAD_CALL")
    public ResponseEntity<Void> updateCallLogByProviderId(
            @PathVariable UUID leadId,
            @PathVariable String externalId,
            @Valid @RequestBody LeadUpdateCallLog request
    ) {
        leadCallWriteService.updateCallLog(leadId, externalId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/log/external")
    @RequirePermission(permissionName = "CREATE_LEAD_CALL")
    public ResponseEntity<CreateExternalCallLogResponse> createExternalCallLog(
            @PathVariable UUID leadId,
            @Valid @RequestBody CreateExternalCallLogRequest request
    ) {
        CreateExternalCallLogResponse response = leadCallWriteService.createExternalCallLog(leadId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

