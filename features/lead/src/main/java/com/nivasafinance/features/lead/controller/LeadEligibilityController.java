package com.nivasafinance.features.lead.controller;

import com.nivasafinance.common.annotations.RequirePermission;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.lead.dto.LeadBREEligibilityDetailResponse;
import com.nivasafinance.features.lead.dto.LeadBREResultExecuteResponse;
import com.nivasafinance.features.lead.service.LeadEligibilityReadService;
import com.nivasafinance.features.lead.service.LeadEligibilityWriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.V1 + "/leads/{leadIdentifier}/bre/eligibility")
@RequiredArgsConstructor
public class LeadEligibilityController {

    private final LeadEligibilityWriteService leadEligibilityWriteService;
    private final LeadEligibilityReadService leadEligibilityReadService;

    @PostMapping("/execute")
    @RequirePermission(permissionName = "LEAD_ELIGIBILITY")
    public ResponseEntity<LeadBREResultExecuteResponse> execute(@PathVariable UUID leadIdentifier) {
        LeadBREResultExecuteResponse response = leadEligibilityWriteService.executeEligibility(leadIdentifier);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/latest")
    @RequirePermission(permissionName = "LEAD_ELIGIBILITY")
    public ResponseEntity<LeadBREEligibilityDetailResponse> getLatest(@PathVariable UUID leadIdentifier) {
        return leadEligibilityReadService.getLatestEligibilityDetail(leadIdentifier)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
