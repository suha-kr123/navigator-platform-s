package com.nivasafinance.externals.whatsapp.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppLeadRequest;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppLeadResponse;
import com.nivasafinance.externals.whatsapp.service.WhatsAppLeadService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.EXTERNAL_V1 + "/whatsapp")
@AllArgsConstructor
public class WhatsAppLeadController {

    private final WhatsAppLeadService whatsAppLeadService;

    @PostMapping("/leads")
    public ResponseEntity<WhatsAppLeadResponse> createOrGetLead(@Valid @RequestBody WhatsAppLeadRequest request) {
        WhatsAppLeadResponse response = whatsAppLeadService.createOrGetLead(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
