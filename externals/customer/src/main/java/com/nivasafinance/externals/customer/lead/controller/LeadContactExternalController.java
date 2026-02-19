package com.nivasafinance.externals.customer.lead.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.common.dto.IdentifierData;
import com.nivasafinance.common.dto.IdentifierRequest;
import com.nivasafinance.features.lead.dto.RecordCbConsentResponse;
import com.nivasafinance.features.lead.dto.UpdateContactNameRequest;
import com.nivasafinance.features.lead.service.LeadContactWriteService;
import com.nivasafinance.features.lead.service.LeadCreditBureauWriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.OPEN_API_V1 + "/customer/leads/{leadId}/contacts/{contactIdentifier}")
@RequiredArgsConstructor
public class LeadContactExternalController {

    private final LeadContactWriteService leadContactWriteService;
    private final LeadCreditBureauWriteService leadCreditBureauWriteService;

    @PatchMapping("/name")
    public ResponseEntity<Void> updateName(
            @PathVariable UUID leadId,
            @PathVariable UUID contactIdentifier,
            @Valid @RequestBody UpdateContactNameRequest request) {
        leadContactWriteService.updateContactName(leadId, contactIdentifier, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/identifiers")
    public ResponseEntity<IdentifierData> addIdentifier(
            @PathVariable UUID leadId,
            @PathVariable UUID contactIdentifier,
            @Valid @RequestBody IdentifierRequest request) {
        IdentifierData identifier = leadContactWriteService.addIdentifier(leadId, contactIdentifier, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(identifier);
    }

    @PostMapping("/credit-bureau/consent")
    public ResponseEntity<RecordCbConsentResponse> recordCbConsent(
            @PathVariable UUID leadId,
            @PathVariable UUID contactIdentifier) {
        RecordCbConsentResponse response = leadCreditBureauWriteService.recordCbConsentReceived(leadId, contactIdentifier);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
