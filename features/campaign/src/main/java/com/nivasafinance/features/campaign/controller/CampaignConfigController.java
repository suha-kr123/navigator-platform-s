package com.nivasafinance.features.campaign.controller;

import com.nivasafinance.common.annotations.RequirePermission;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.campaign.dto.CampaignConfigDetailedResponse;
import com.nivasafinance.features.campaign.dto.CampaignConfigResponse;
import com.nivasafinance.features.campaign.enums.CampaignConfigStatus;
import com.nivasafinance.features.campaign.service.CampaignConfigReadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.V1 + "/campaign-configs")
@RequiredArgsConstructor
public class CampaignConfigController {
    private final CampaignConfigReadService campaignConfigReadService;

    @GetMapping
    @RequirePermission(permissionName = "READ_CAMPAIGN_CONFIG")
    public ResponseEntity<PaginatedResponse<CampaignConfigResponse>> getAllCampaignConfigs(
            @RequestParam(required = false) List<CampaignConfigStatus> statuses,
            @Valid @ModelAttribute PaginationRequest paginationRequest) {
        PaginatedResponse<CampaignConfigResponse> response = 
                campaignConfigReadService.getAllCampaignConfigs(statuses, paginationRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{identifier}")
    @RequirePermission(permissionName = "READ_CAMPAIGN_CONFIG")
    public ResponseEntity<CampaignConfigDetailedResponse> getCampaignConfigByIdentifier(
            @PathVariable UUID identifier) {
        CampaignConfigDetailedResponse response = 
                campaignConfigReadService.getCampaignConfigByIdentifier(identifier);
        return ResponseEntity.ok(response);
    }
}
