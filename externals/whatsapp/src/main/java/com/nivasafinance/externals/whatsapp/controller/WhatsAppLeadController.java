package com.nivasafinance.externals.whatsapp.controller;

import com.nivasafinance.common.annotations.RequirePermission;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppLeadResponse;
import com.nivasafinance.externals.whatsapp.service.WhatsAppLeadService;
import com.nivasafinance.features.lead.dto.CreateLeadRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.V1 + "/externals/whatsapp")
@AllArgsConstructor
public class WhatsAppLeadController {

    private final WhatsAppLeadService whatsAppLeadService;

    @PostMapping("/leads")
    @RequirePermission(permissionName = "CREATE_LEAD")
    public ResponseEntity<WhatsAppLeadResponse> createOrGetLead(@Valid @RequestBody CreateLeadRequest request) {
        WhatsAppLeadResponse response = whatsAppLeadService.createOrGetLead(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
