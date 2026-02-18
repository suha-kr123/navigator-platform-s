package com.nivasafinance.externals.creditbureau.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.externals.creditbureau.dto.ContactCreditBureauResponse;
import com.nivasafinance.externals.creditbureau.service.CreditBureauExternalService;
import com.nivasafinance.features.consent.dto.AcceptConsentResponse;
import com.nivasafinance.features.consent.dto.WithdrawConsentResponse;
import com.nivasafinance.features.lead.dto.EnquiryConsentStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.OPEN_API_V1 + "/leads")
@RequiredArgsConstructor
@Slf4j
public class CreditBureauExternalController {

    private final CreditBureauExternalService creditBureauExternalService;

    @GetMapping("/{leadIdentifier}/contacts/{contactIdentifier}")
    public ResponseEntity<ContactCreditBureauResponse> getContact(
            @PathVariable UUID leadIdentifier,
            @PathVariable UUID contactIdentifier) {
        ContactCreditBureauResponse response = creditBureauExternalService.getContactByLeadAndContact(leadIdentifier, contactIdentifier);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{leadIdentifier}/contacts/{contactIdentifier}/enquiry/{enquiryIdentifier}/consent-status")
    public ResponseEntity<EnquiryConsentStatusResponse> getConsentStatus(
            @PathVariable UUID leadIdentifier,
            @PathVariable UUID contactIdentifier,
            @PathVariable UUID enquiryIdentifier) {
        Optional<EnquiryConsentStatusResponse> consentStatus = creditBureauExternalService.getConsentStatus(leadIdentifier, contactIdentifier, enquiryIdentifier);
        return consentStatus.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{leadIdentifier}/contacts/{contactIdentifier}/enquiry/{enquiryIdentifier}/consent/{consentIdentifier}/accept")
    public ResponseEntity<AcceptConsentResponse> acceptConsent(
            @PathVariable UUID leadIdentifier,
            @PathVariable UUID contactIdentifier,
            @PathVariable UUID enquiryIdentifier,
            @PathVariable UUID consentIdentifier) {
        AcceptConsentResponse response = creditBureauExternalService.acceptConsent(leadIdentifier, contactIdentifier, enquiryIdentifier, consentIdentifier);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{leadIdentifier}/contacts/{contactIdentifier}/enquiry/{enquiryIdentifier}/consent/{consentIdentifier}/withdraw")
    public ResponseEntity<WithdrawConsentResponse> withdrawConsent(
            @PathVariable UUID leadIdentifier,
            @PathVariable UUID contactIdentifier,
            @PathVariable UUID enquiryIdentifier,
            @PathVariable UUID consentIdentifier) {
        WithdrawConsentResponse response = creditBureauExternalService.withdrawConsent(leadIdentifier, contactIdentifier, enquiryIdentifier, consentIdentifier);
        return ResponseEntity.ok(response);
    }
}
