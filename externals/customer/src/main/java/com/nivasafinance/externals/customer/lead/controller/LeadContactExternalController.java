package com.nivasafinance.externals.customer.lead.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.common.dto.IdentifierData;
import com.nivasafinance.externals.customer.lead.dto.PanRequest;
import com.nivasafinance.externals.customer.lead.dto.PhoneNumberRequest;
import com.nivasafinance.externals.customer.lead.service.LeadContactExternalService;
import com.nivasafinance.features.lead.dto.LeadContactResponse;
import com.nivasafinance.features.lead.dto.RecordCbConsentResponse;
import com.nivasafinance.features.lead.dto.UpdateContactNameRequest;
import com.nivasafinance.features.lead.service.LeadContactReadService;
import com.nivasafinance.features.lead.service.LeadContactWriteService;
import com.nivasafinance.features.lead.service.LeadCreditBureauWriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.OPEN_API_V1 + "/customer/leads/{leadId}/contacts")
@RequiredArgsConstructor
public class LeadContactExternalController {

    private final LeadContactReadService leadContactReadService;
    private final LeadContactWriteService leadContactWriteService;
    private final LeadContactExternalService leadContactExternalService;
    private final LeadCreditBureauWriteService leadCreditBureauWriteService;

    @GetMapping
    public ResponseEntity<List<LeadContactResponse>> getContacts(@PathVariable UUID leadId) {
        List<LeadContactResponse> contacts = leadContactExternalService.getContacts(leadId);
        return ResponseEntity.ok(contacts);
    }

    @GetMapping("/{contactIdentifier}")
    public ResponseEntity<LeadContactResponse> getContact(
            @PathVariable UUID leadId,
            @PathVariable UUID contactIdentifier) {
        return ResponseEntity.ok(leadContactReadService.getContactById(leadId, contactIdentifier));
    }

    @GetMapping("/{contactIdentifier}/pan")
    public ResponseEntity<IdentifierData> getPan(
            @PathVariable UUID leadId,
            @PathVariable UUID contactIdentifier) {
        return leadContactExternalService.getPan(leadId, contactIdentifier)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{contactIdentifier}/name")
    public ResponseEntity<Void> updateName(
            @PathVariable UUID leadId,
            @PathVariable UUID contactIdentifier,
            @Valid @RequestBody UpdateContactNameRequest request) {
        leadContactWriteService.updateContactName(leadId, contactIdentifier, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{contactIdentifier}/phone-number")
    public ResponseEntity<Void> addPhoneNumber(
            @PathVariable UUID leadId,
            @PathVariable UUID contactIdentifier,
            @Valid @RequestBody PhoneNumberRequest request) {
        leadContactWriteService.addPhoneNumber(
                leadId,
                contactIdentifier,
                request.getNumber(),
                request.getIsPrimary(),
                request.getIsWhatsappAvailable());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{contactIdentifier}/pan")
    public ResponseEntity<IdentifierData> addOrReplacePan(
            @PathVariable UUID leadId,
            @PathVariable UUID contactIdentifier,
            @Valid @RequestBody PanRequest request) {
        IdentifierData identifier = leadContactExternalService.addOrReplacePan(leadId, contactIdentifier, request.getPan());
        return ResponseEntity.ok(identifier);
    }

    @PostMapping("/{contactIdentifier}/credit-bureau/consent")
    public ResponseEntity<RecordCbConsentResponse> recordCbConsent(
            @PathVariable UUID leadId,
            @PathVariable UUID contactIdentifier) {
        RecordCbConsentResponse response = leadCreditBureauWriteService.recordCbConsentReceived(leadId, contactIdentifier);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
