package com.nivasafinance.externals.gallabox.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.externals.gallabox.dto.WhatsAppLeadUpdateRequest;
import com.nivasafinance.externals.gallabox.service.WhatsAppLeadUpdateService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.EXTERNAL_V1 + "/whatsapp")
@AllArgsConstructor
public class WhatsAppLeadUpdateController {

    private final WhatsAppLeadUpdateService whatsAppLeadUpdateService;

    @PatchMapping("/leads/update")
    public ResponseEntity<Void> updateLead(@RequestBody WhatsAppLeadUpdateRequest request) {
        whatsAppLeadUpdateService.updateLead(request);
        return ResponseEntity.noContent().build();
    }
}
