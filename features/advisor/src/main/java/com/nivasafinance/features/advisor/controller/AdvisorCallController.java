package com.nivasafinance.features.advisor.controller;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.advisor.dto.*;
import com.nivasafinance.features.advisor.service.AdvisorCallReadService;
import com.nivasafinance.features.advisor.service.AdvisorCallWriteService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.V1 + "/advisors/{advisorId}/call")
@AllArgsConstructor
public class AdvisorCallController {

    private final AdvisorCallWriteService advisorCallWriteService;
    private final AdvisorCallReadService advisorCallReadService;

    @PostMapping
    public ResponseEntity<CreateAdvisorCallResponse> callAdvisorPerson(
            @PathVariable UUID advisorId,
            @Valid @RequestBody CreateAdvisorCallRequest request
    ) {
        CreateAdvisorCallResponse response = advisorCallWriteService.callPerson(advisorId, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/logs")
    public ResponseEntity<PaginatedResponse<AdvisorCallLogResponse>> getCallLogs(
            @PathVariable UUID advisorId,
            @Valid PaginationRequest paginationRequest
    ) {
        PaginatedResponse<AdvisorCallLogResponse> response = advisorCallReadService.getCallLogs(advisorId, paginationRequest);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/external/{externalId}")
    public ResponseEntity<Void> updateCallLogByProviderId(
            @PathVariable UUID advisorId,
            @PathVariable String externalId,
            @Valid @RequestBody AdvisorUpdateCallLog request
    ) {
        advisorCallWriteService.updateCallLog(advisorId, externalId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/log/external")
    public ResponseEntity<CreateExternalCallLogResponse> createExternalCallLog(
            @PathVariable UUID advisorId,
            @Valid @RequestBody CreateExternalCallLogRequest request
    ) {
        CreateExternalCallLogResponse response = advisorCallWriteService.createExternalCallLog(advisorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
