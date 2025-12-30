package com.nivasafinance.features.leadactivity.controller;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.common.annotations.RequirePermission;
import com.nivasafinance.features.leadactivity.dto.LeadActivityResponse;
import com.nivasafinance.features.leadactivity.service.LeadActivityReadService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.V1 + "/leads/{leadIdentifier}/activities")
@AllArgsConstructor
public class LeadActivityController {

    private final LeadActivityReadService leadActivityReadService;

    @GetMapping
    @RequirePermission(permissionName = "READ_LEAD")
    public ResponseEntity<PaginatedResponse<LeadActivityResponse>> getLeadActivities(
            @PathVariable UUID leadIdentifier,
            @Valid PaginationRequest paginationRequest) {
        PaginatedResponse<LeadActivityResponse> activities =
                leadActivityReadService.getActivities(leadIdentifier, paginationRequest);
        return ResponseEntity.ok(activities);
    }
}


