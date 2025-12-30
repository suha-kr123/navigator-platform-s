package com.nivasafinance.features.advisor.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.advisor.dto.SourcingDetailsResponse;
import com.nivasafinance.features.advisor.dto.UpdateSourcingDetailsRequest;
import com.nivasafinance.features.advisor.service.AdvisorReadService;
import com.nivasafinance.features.advisor.service.AdvisorWriteService;
import com.nivasafinance.common.annotations.RequirePermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.V1 + "/advisors/{identifier}/sourcing-details")
@RequiredArgsConstructor
public class AdvisorSourcingDetailsController {

    private final AdvisorWriteService advisorWriteService;
    private final AdvisorReadService advisorReadService;

    @PutMapping
    @RequirePermission(permissionName = "UPDATE_ADVISOR")
    public ResponseEntity<Void> updateSourcingDetails(
            @PathVariable UUID identifier,
            @Valid @RequestBody UpdateSourcingDetailsRequest request) {
        advisorWriteService.updateSourcingDetails(identifier, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @RequirePermission(permissionName = "READ_ADVISOR")
    public ResponseEntity<SourcingDetailsResponse> getSourcingDetails(
            @PathVariable UUID identifier) {
        SourcingDetailsResponse response = advisorReadService.getSourcingDetails(identifier);
        return ResponseEntity.ok(response);
    }
}

