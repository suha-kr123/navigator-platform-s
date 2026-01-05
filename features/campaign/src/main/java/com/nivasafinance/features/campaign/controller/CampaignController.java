package com.nivasafinance.features.campaign.controller;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.campaign.dto.CampaignDetailedResponse;
import com.nivasafinance.features.campaign.dto.CampaignDraftRequest;
import com.nivasafinance.features.campaign.dto.CampaignDraftResponse;
import com.nivasafinance.features.campaign.dto.CampaignGenerateDocumentRequest;
import com.nivasafinance.features.campaign.dto.CampaignResponse;
import com.nivasafinance.features.campaign.dto.UpdateCampaignDraftRequest;
import com.nivasafinance.features.campaign.service.CampaignReadService;
import com.nivasafinance.features.campaign.service.CampaignWriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.V1 + "/campaigns")
@RequiredArgsConstructor
public class CampaignController {
    private final CampaignReadService campaignReadService;
    private final CampaignWriteService campaignWriteService;

    @PostMapping("/draft")
    public ResponseEntity<CampaignDraftResponse> createCampaignDraft(
            @Valid @RequestBody CampaignDraftRequest request) {
        CampaignDraftResponse response = campaignWriteService.createCampaignDraft(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{identifier}/draft")
    public ResponseEntity<Void> updateCampaignDraft(
            @PathVariable UUID identifier,
            @Valid @RequestBody UpdateCampaignDraftRequest request) {
        campaignWriteService.updateCampaignDraft(identifier, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{identifier}/cancel")
    public ResponseEntity<Void> cancelCampaign(@PathVariable UUID identifier) {
        campaignWriteService.cancelCampaign(identifier);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{identifier}/submit")
    public ResponseEntity<Void> submitCampaign(@PathVariable UUID identifier) {
        campaignWriteService.submitCampaign(identifier);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{identifier}/refresh")
    public ResponseEntity<CampaignDetailedResponse> refreshCampaign(@PathVariable UUID identifier) {
        CampaignDetailedResponse response = campaignWriteService.refreshCampaign(identifier);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<CampaignResponse>> getAllCampaigns(
            @Valid @ModelAttribute PaginationRequest paginationRequest) {
        PaginatedResponse<CampaignResponse> response = 
                campaignReadService.getAllCampaigns(paginationRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{identifier}")
    public ResponseEntity<CampaignDetailedResponse> getCampaignByIdentifier(
            @PathVariable UUID identifier) {
        CampaignDetailedResponse response = 
                campaignReadService.getCampaignByIdentifier(identifier);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{identifier}/generate-document")
    public ResponseEntity<Void> generateDocument(
            @PathVariable UUID identifier,
            @Valid @RequestBody CampaignGenerateDocumentRequest request) {
        campaignWriteService.generateDocument(identifier, request);
        return ResponseEntity.ok().build();
    }
}
