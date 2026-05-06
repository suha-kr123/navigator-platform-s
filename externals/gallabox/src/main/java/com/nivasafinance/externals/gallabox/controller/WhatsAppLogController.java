package com.nivasafinance.externals.gallabox.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.externals.gallabox.dto.WhatsAppLogRequest;
import com.nivasafinance.externals.gallabox.dto.WhatsAppLogResponse;
import com.nivasafinance.externals.gallabox.service.WhatsAppLogService;
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
