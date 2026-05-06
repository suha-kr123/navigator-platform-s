package com.nivasafinance.externals.gallabox.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.externals.gallabox.dto.WhatsAppAdvisorRequest;
import com.nivasafinance.externals.gallabox.dto.WhatsAppAdvisorResponse;
import com.nivasafinance.externals.gallabox.service.WhatsAppAdvisorService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.EXTERNAL_V1 + "/whatsapp")
@AllArgsConstructor
public class WhatsAppAdvisorController {

    private final WhatsAppAdvisorService whatsAppAdvisorService;

    @PostMapping("/advisors")
    public ResponseEntity<WhatsAppAdvisorResponse> createOrGetAdvisor(@Valid @RequestBody WhatsAppAdvisorRequest request) {
        WhatsAppAdvisorResponse response = whatsAppAdvisorService.createOrGetAdvisor(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
