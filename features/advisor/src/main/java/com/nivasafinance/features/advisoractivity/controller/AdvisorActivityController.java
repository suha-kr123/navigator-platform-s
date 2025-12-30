package com.nivasafinance.features.advisoractivity.controller;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.advisoractivity.dto.AdvisorActivityResponse;
import com.nivasafinance.features.advisoractivity.service.AdvisorActivityReadService;
import com.nivasafinance.common.annotations.RequirePermission;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.V1 + "/advisors/{advisorIdentifier}/activities")
@AllArgsConstructor
public class AdvisorActivityController {

    private final AdvisorActivityReadService advisorActivityReadService;

    @GetMapping
    @RequirePermission(permissionName = "READ_ADVISOR")
    public ResponseEntity<PaginatedResponse<AdvisorActivityResponse>> getAdvisorActivities(
            @PathVariable UUID advisorIdentifier,
            @Valid PaginationRequest paginationRequest) {
        PaginatedResponse<AdvisorActivityResponse> activities =
                advisorActivityReadService.getActivities(advisorIdentifier, paginationRequest);
        return ResponseEntity.ok(activities);
    }
}

