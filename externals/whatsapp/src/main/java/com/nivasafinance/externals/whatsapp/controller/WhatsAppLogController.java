package com.nivasafinance.externals.whatsapp.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppLogRequest;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppLogResponse;
import com.nivasafinance.externals.whatsapp.service.WhatsAppLogService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.EXTERNAL_V1 + "/whatsapp")
@AllArgsConstructor
public class WhatsAppLogController {

    private final WhatsAppLogService whatsAppLogService;

    @PostMapping("/log")
    public ResponseEntity<WhatsAppLogResponse> createWhatsAppLog(@Valid @RequestBody WhatsAppLogRequest request) {
        WhatsAppLogResponse response = whatsAppLogService.createWhatsAppLog(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
