package com.nivasafinance.externals.whatsapp.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppStatusTrackerRequest;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppStatusTrackerResponse;
import com.nivasafinance.externals.whatsapp.service.WhatsAppStatusTrackerService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.EXTERNAL_V1 + "/whatsapp")
@AllArgsConstructor
public class WhatsAppStatusTrackerController {

    private final WhatsAppStatusTrackerService whatsAppStatusTrackerService;

    @PostMapping("/status-tracker")
    public ResponseEntity<WhatsAppStatusTrackerResponse> trackStatus(
            @Valid @RequestBody WhatsAppStatusTrackerRequest request) {
        WhatsAppStatusTrackerResponse response = whatsAppStatusTrackerService.trackStatus(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
