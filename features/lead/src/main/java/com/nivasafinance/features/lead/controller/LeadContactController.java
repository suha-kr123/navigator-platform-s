package com.nivasafinance.features.lead.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.AddressRequest;
import com.nivasafinance.common.dto.IdentifierData;
import com.nivasafinance.common.dto.IdentifierRequest;
import com.nivasafinance.features.lead.dto.AddressIdentifierResponse;
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

    // Address Endpoints

    @PostMapping("/{contactIdentifier}/address")
    public ResponseEntity<AddressIdentifierResponse> addAddress(
            @PathVariable UUID contactIdentifier,
            @Valid @RequestBody AddressRequest request) {
        String addressId = leadContactWriteService.addAddress(contactIdentifier, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AddressIdentifierResponse(addressId));
    }

    @GetMapping("/{contactIdentifier}/addresses")
    public ResponseEntity<List<AddressData>> getAddresses(@PathVariable UUID contactIdentifier) {
        List<AddressData> addresses = leadContactReadService.getAddresses(contactIdentifier);
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/{contactIdentifier}/address/{addressId}")
    public ResponseEntity<AddressData> getAddress(
            @PathVariable UUID contactIdentifier,
            @PathVariable String addressId) {
        AddressData address = leadContactReadService.getAddress(contactIdentifier, addressId);
        return ResponseEntity.ok(address);
    }

    @PutMapping("/{contactIdentifier}/address/{addressId}")
    public ResponseEntity<Void> updateAddress(
            @PathVariable UUID contactIdentifier,
            @PathVariable String addressId,
            @Valid @RequestBody AddressRequest request) {
        leadContactWriteService.updateAddress(contactIdentifier, addressId, request);
        return ResponseEntity.noContent().build();
    }

    // Identifier Endpoints

    @PostMapping("/{contactIdentifier}/identifiers")
    public ResponseEntity<IdentifierData> addIdentifier(
            @PathVariable UUID leadId,
            @PathVariable UUID contactIdentifier,
            @Valid @RequestBody IdentifierRequest request) {
        IdentifierData identifier = leadContactWriteService.addIdentifier(leadId, contactIdentifier, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(identifier);
    }

    @GetMapping("/{contactIdentifier}/identifiers")
    public ResponseEntity<List<IdentifierData>> getIdentifiers(
            @PathVariable UUID leadId,
            @PathVariable UUID contactIdentifier) {
        List<IdentifierData> identifiers = leadContactReadService.getIdentifiers(leadId, contactIdentifier);
        return ResponseEntity.ok(identifiers);
    }

    @GetMapping("/{contactIdentifier}/identifiers/{identifierId}")
    public ResponseEntity<IdentifierData> getIdentifier(
            @PathVariable UUID leadId,
            @PathVariable UUID contactIdentifier,
            @PathVariable UUID identifierId) {
        IdentifierData identifier = leadContactReadService.getIdentifier(leadId, contactIdentifier, identifierId);
        return ResponseEntity.ok(identifier);
    }

    @PutMapping("/{contactIdentifier}/identifiers/{identifierId}")
    public ResponseEntity<Void> updateIdentifier(
            @PathVariable UUID leadId,
            @PathVariable UUID contactIdentifier,
            @PathVariable UUID identifierId,
            @Valid @RequestBody IdentifierRequest request) {
        leadContactWriteService.updateIdentifier(leadId, contactIdentifier, identifierId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{contactIdentifier}/identifiers/{identifierId}")
    public ResponseEntity<Void> deleteIdentifier(
            @PathVariable UUID leadId,
            @PathVariable UUID contactIdentifier,
            @PathVariable UUID identifierId) {
        leadContactWriteService.deleteIdentifier(leadId, contactIdentifier, identifierId);
        return ResponseEntity.noContent().build();
    }

}








