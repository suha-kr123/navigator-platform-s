package com.nivasafinance.features.lead.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.lead.dto.LeadContactResponse;
import com.nivasafinance.features.lead.dto.CreateLeadContactRequest;
import com.nivasafinance.features.lead.dto.UpdateLeadContactRequest;
import com.nivasafinance.features.lead.service.LeadContactReadService;
import com.nivasafinance.features.lead.service.LeadContactWriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.V1 + "/leads/{leadId}/contacts")
@RequiredArgsConstructor
public class LeadContactController {

    private final LeadContactWriteService leadContactWriteService;
    private final LeadContactReadService leadContactReadService;

    @PostMapping
    public ResponseEntity<Void> createContact(
            @PathVariable UUID leadId,
            @Valid @RequestBody CreateLeadContactRequest request) {
        leadContactWriteService.createContact(leadId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{contactIdentifier}")
    public ResponseEntity<Void> updateContact(
            @PathVariable UUID leadId,
            @PathVariable UUID contactIdentifier,
            @Valid @RequestBody UpdateLeadContactRequest request) {
        leadContactWriteService.updateContact(leadId, contactIdentifier, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{contactIdentifier}")
    public ResponseEntity<Void> deleteContact(
            @PathVariable UUID leadId,
            @PathVariable UUID contactIdentifier) {
        leadContactWriteService.deleteContact(leadId, contactIdentifier);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<LeadContactResponse>> getContacts(@PathVariable UUID leadId) {
        List<LeadContactResponse> contacts = leadContactReadService.getContacts(leadId);
        return ResponseEntity.ok(contacts);
    }

    @GetMapping("/{contactIdentifier}")
    public ResponseEntity<LeadContactResponse> getContactById(
            @PathVariable UUID leadId,
            @PathVariable UUID contactIdentifier) {
        LeadContactResponse contact = leadContactReadService.getContactById(leadId, contactIdentifier);
        return ResponseEntity.ok(contact);
    }
}

