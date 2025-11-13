package com.nivasafinance.features.lead.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.call.dto.InitiateCallResponse;
import com.nivasafinance.features.lead.dto.CreateLeadCallRequest;
import com.nivasafinance.features.lead.dto.CreateLeadCallResponse;
import com.nivasafinance.features.lead.service.LeadCallWriteService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.V1 + "/leads/{leadId}/call")
@AllArgsConstructor
public class LeadCallController {

    private final LeadCallWriteService leadCallWriteService;

    @PostMapping
    public ResponseEntity<CreateLeadCallResponse> callLeadContact(
            @PathVariable UUID leadId,
            @Valid @RequestBody CreateLeadCallRequest request
    ) {
        CreateLeadCallResponse response = leadCallWriteService.callContact(leadId, request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}

