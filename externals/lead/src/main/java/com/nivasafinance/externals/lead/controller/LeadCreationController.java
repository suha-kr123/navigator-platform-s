package com.nivasafinance.externals.lead.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.common.annotations.RequirePermission;
import com.nivasafinance.externals.lead.dto.CreateLeadRequest;
import com.nivasafinance.externals.lead.dto.CreateLeadResponse;
import com.nivasafinance.externals.lead.service.LeadCreationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.V1 + "/externals/leads")
@AllArgsConstructor
public class LeadCreationController {

    private final LeadCreationService leadCreationService;

    @PostMapping
    @RequirePermission(permissionName = "CREATE_LEAD")
    public ResponseEntity<CreateLeadResponse> createLead(@Valid @RequestBody CreateLeadRequest request) {
        CreateLeadResponse response = leadCreationService.createLead(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
