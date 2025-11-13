package com.nivasafinance.features.advisor.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.advisor.dto.*;
import com.nivasafinance.features.advisor.service.AdvisorReadService;
import com.nivasafinance.features.advisor.service.AdvisorWriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping(ApiConstants.V1 + "/advisors")
@RequiredArgsConstructor
public class AdvisorController {

    private final AdvisorWriteService advisorWriteService;
    private final AdvisorReadService advisorReadService;

    @GetMapping("/template")
    public ResponseEntity<AdvisorTemplateResponse> getAdvisorTemplate() {
        AdvisorTemplateResponse response = advisorReadService.getAdvisorTemplate();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<IdentifierResponse> createAdvisor(@Valid @RequestBody CreateAdvisorRequest request) {
        UUID identifier = advisorWriteService.createAdvisor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new IdentifierResponse(identifier));
    }

    @GetMapping("/{identifier}")
    public ResponseEntity<AdvisorResponse> getAdvisorByIdentifier(@PathVariable UUID identifier) {
        AdvisorResponse response = advisorReadService.getAdvisorByIdentifier(identifier);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{identifier}")
    public ResponseEntity<Void> updateAdvisor(
            @PathVariable UUID identifier,
            @Valid @RequestBody UpdateAdvisorRequest request) {
        advisorWriteService.updateAdvisor(identifier, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{identifier}/qualification-details")
    public ResponseEntity<Void> updateQualificationDetails(
            @PathVariable UUID identifier,
            @Valid @RequestBody UpdateQualificationDetailsRequest request) {
        advisorWriteService.updateQualificationDetails(identifier, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{identifier}/occupation-details")
    public ResponseEntity<Void> updateOccupationDetails(
            @PathVariable UUID identifier,
            @Valid @RequestBody UpdateOccupationDetailsRequest request) {
        advisorWriteService.updateOccupationDetails(identifier, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{identifier}/reject")
    public ResponseEntity<IdentifierResponse> rejectAdvisor(
            @PathVariable UUID identifier,
            @Valid @RequestBody RejectAdvisorRequest request) {
        UUID resultIdentifier = advisorWriteService.rejectAdvisor(identifier, request);
        return ResponseEntity.ok(new IdentifierResponse(resultIdentifier));
    }

    @PostMapping("/{identifier}/activate")
    public ResponseEntity<IdentifierResponse> activateAdvisor(
            @PathVariable UUID identifier) {
        UUID resultIdentifier = advisorWriteService.activateAdvisor(identifier);
        return ResponseEntity.ok(new IdentifierResponse(resultIdentifier));
    }

}
