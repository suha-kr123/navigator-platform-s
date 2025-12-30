package com.nivasafinance.features.leadlender.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.common.annotations.RequirePermission;
import com.nivasafinance.features.leadlender.dto.CreateLeadLenderRequest;
import com.nivasafinance.features.leadlender.dto.CreateLeadLenderResponse;
import com.nivasafinance.features.leadlender.dto.LeadLenderResponse;
import com.nivasafinance.features.leadlender.dto.RejectLeadLenderRequest;
import com.nivasafinance.features.leadlender.dto.UpdateLeadLenderRequest;
import com.nivasafinance.features.leadlender.service.LeadLenderReadService;
import com.nivasafinance.features.leadlender.service.LeadLenderWriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.V1 + "/leads/{leadIdentifier}/lenders")
@RequiredArgsConstructor
public class LeadLenderController {

    private final LeadLenderReadService leadLenderReadService;
    private final LeadLenderWriteService leadLenderWriteService;

    @PostMapping
    @RequirePermission(permissionName = "CREATE_LEAD_LENDERS")
    public CreateLeadLenderResponse createLeadLender(
        @PathVariable UUID leadIdentifier,
        @Valid @RequestBody CreateLeadLenderRequest request
    ) {
        return leadLenderWriteService.createLeadLender(leadIdentifier, request);
    }

    @PutMapping("/{lenderIdentifier}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequirePermission(permissionName = "UPDATE_LEAD_LENDERS")
    public void updateLeadLender(
        @PathVariable UUID leadIdentifier,
        @PathVariable UUID lenderIdentifier,
        @Valid @RequestBody UpdateLeadLenderRequest request
    ) {
        leadLenderWriteService.updateLeadLender(leadIdentifier, lenderIdentifier, request);
    }

    @PostMapping("/{lenderIdentifier}/reject")
    @ResponseStatus(HttpStatus.OK)
    @RequirePermission(permissionName = "REJECT_LEAD_LENDERS")
    public void rejectLeadLender(
        @PathVariable UUID leadIdentifier,
        @PathVariable UUID lenderIdentifier,
        @Valid @RequestBody RejectLeadLenderRequest request
    ) {
        leadLenderWriteService.rejectLeadLender(leadIdentifier, lenderIdentifier, request);
    }

    @PostMapping("/{lenderIdentifier}/submit")
    @ResponseStatus(HttpStatus.OK)
    @RequirePermission(permissionName = "SUBMIT_LEAD_LENDERS")
    public void submitLeadLender(
        @PathVariable UUID leadIdentifier,
        @PathVariable UUID lenderIdentifier
    ) {
        leadLenderWriteService.submitLeadLender(leadIdentifier, lenderIdentifier);
    }

    @GetMapping("/{lenderIdentifier}")
    @RequirePermission(permissionName = "READ_LEAD_LENDERS")
    public LeadLenderResponse getLeadLenderByIdentifier(
        @PathVariable UUID leadIdentifier,
        @PathVariable UUID lenderIdentifier
    ) {
        return leadLenderReadService.getLeadLenderByIdentifier(leadIdentifier, lenderIdentifier);
    }

    @GetMapping("/all")
    @RequirePermission(permissionName = "READ_LEAD_LENDERS")
    public List<LeadLenderResponse> getLeadLenders(
        @PathVariable UUID leadIdentifier,
        @RequestParam(required = false) List<String> status
    ) {
        return leadLenderReadService.getLeadLenders(leadIdentifier, status);
    }
}

