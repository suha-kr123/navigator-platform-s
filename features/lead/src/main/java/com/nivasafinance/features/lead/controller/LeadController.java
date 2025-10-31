package com.nivasafinance.features.lead.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.lead.dto.CreateLeadRequest;
import com.nivasafinance.features.lead.dto.CreateLeadResponse;
import com.nivasafinance.features.lead.dto.LeadResponse;
import com.nivasafinance.features.lead.service.LeadReadService;
import com.nivasafinance.features.lead.service.LeadWriteService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.V1 + "/leads")
@AllArgsConstructor
public class LeadController {

    private final LeadWriteService leadWriteService;
    private final LeadReadService leadReadService;

    @PostMapping
    public ResponseEntity<CreateLeadResponse> createLead(@Valid @RequestBody CreateLeadRequest request) {
        CreateLeadResponse response = leadWriteService.createLead(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{leadId}")
    public ResponseEntity<LeadResponse> getLeads(@PathVariable UUID leadId) {
        return ResponseEntity.status(HttpStatus.OK).body(leadReadService.getLeadByIdentifier(leadId));
    }
}
