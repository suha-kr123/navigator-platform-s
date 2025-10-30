package com.nivasafinance.features.leadlender.controller;

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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lead/{leadId}/lender")
@RequiredArgsConstructor
public class LeadLenderController {

    private final LeadLenderReadService leadLenderReadService;
    private final LeadLenderWriteService leadLenderWriteService;

    @PostMapping
    public CreateLeadLenderResponse createLeadLender(
        @PathVariable UUID leadId,
        @Valid @RequestBody CreateLeadLenderRequest request
    ) {
        return leadLenderWriteService.createLeadLender(leadId, request);
    }

    @PatchMapping("/{lenderIdentifier}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateLeadLender(
        @PathVariable UUID leadId,
        @PathVariable UUID lenderIdentifier,
        @Valid @RequestBody UpdateLeadLenderRequest request
    ) {
        leadLenderWriteService.updateLeadLender(leadId, lenderIdentifier, request);
    }

    @PostMapping("/{lenderIdentifier}/reject")
    @ResponseStatus(HttpStatus.OK)
    public void rejectLeadLender(
        @PathVariable UUID leadId,
        @PathVariable UUID lenderIdentifier,
        @Valid @RequestBody RejectLeadLenderRequest request
    ) {
        leadLenderWriteService.rejectLeadLender(leadId, lenderIdentifier, request);
    }

    @GetMapping("/{lenderIdentifier}")
    public LeadLenderResponse getLeadLenderByIdentifier(
        @PathVariable UUID leadId,
        @PathVariable UUID lenderIdentifier
    ) {
        return leadLenderReadService.getLeadLenderByIdentifier(leadId, lenderIdentifier);
    }

    @GetMapping("/all")
    public List<LeadLenderResponse> getLeadLenders(
        @PathVariable UUID leadId,
        @RequestParam(required = false) List<String> status
    ) {
        return leadLenderReadService.getLeadLenders(leadId, status);
    }
}

