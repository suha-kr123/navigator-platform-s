package com.nivasafinance.features.lead.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.consent.dto.AcceptConsentResponse;
import com.nivasafinance.features.consent.dto.WithdrawConsentResponse;
import com.nivasafinance.features.consent.enums.ConsentStatus;
import com.nivasafinance.features.creditbureau.dto.*;
import com.nivasafinance.features.lead.dto.EnquiryConsentStatusResponse;
import com.nivasafinance.features.lead.dto.InitiateCbEnquiryResponse;
import com.nivasafinance.features.lead.service.LeadCreditBureauReadService;
import com.nivasafinance.features.lead.service.LeadCreditBureauWriteService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.V1 + "/leads/{leadIdentifier}/contact/{contactIdentifier}")
@AllArgsConstructor
public class LeadCreditBureauController {

    private final LeadCreditBureauWriteService leadCreditBureauWriteService;
    private final LeadCreditBureauReadService leadCreditBureauReadService;

    @PostMapping("/enquiry/initiate")
    public ResponseEntity<InitiateCbEnquiryResponse> initiateEnquiry(
            @PathVariable UUID leadIdentifier,
            @PathVariable UUID contactIdentifier) {
        InitiateCbEnquiryResponse response = leadCreditBureauWriteService.initiateEnquiry(leadIdentifier, contactIdentifier);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/enquiry/{enquiryIdentifier}/consent/{consentIdentifier}/accept")
    public ResponseEntity<AcceptConsentResponse> acceptConsent(
            @PathVariable UUID leadIdentifier,
            @PathVariable UUID contactIdentifier,
            @PathVariable UUID enquiryIdentifier,
            @PathVariable UUID consentIdentifier) {
        leadCreditBureauWriteService.acceptConsent(leadIdentifier, contactIdentifier, enquiryIdentifier, consentIdentifier);
        return ResponseEntity.ok(AcceptConsentResponse.builder()
                .consentStatus(ConsentStatus.RECEIVED)
                .build());
    }

    @PostMapping("/enquiry/{enquiryIdentifier}/consent/{consentIdentifier}/withdraw")
    public ResponseEntity<WithdrawConsentResponse> withdrawConsent(
            @PathVariable UUID leadIdentifier,
            @PathVariable UUID contactIdentifier,
            @PathVariable UUID enquiryIdentifier,
            @PathVariable UUID consentIdentifier) {
        leadCreditBureauWriteService.withdrawConsent(leadIdentifier, contactIdentifier, enquiryIdentifier, consentIdentifier);
        return ResponseEntity.ok(WithdrawConsentResponse.builder()
                .consentStatus(ConsentStatus.REQUEST_FOR_WITHDRAWAL)
                .build());
    }

    @GetMapping("/enquiry/{enquiryIdentifier}/customer-enquiry")
    public ResponseEntity<List<CustomerEnquiryResponse>> getCustomerEnquiry(
            @PathVariable UUID leadIdentifier,
            @PathVariable UUID contactIdentifier,
            @PathVariable UUID enquiryIdentifier) {
        List<CustomerEnquiryResponse> customerEnquiries = leadCreditBureauReadService.getCustomerEnquiryByEnquiryIdentifier(leadIdentifier, contactIdentifier, enquiryIdentifier);
        return ResponseEntity.ok(customerEnquiries);
    }

    @GetMapping("/enquiry/{enquiryIdentifier}/summary")
    public ResponseEntity<SummaryResponse> getSummary(
            @PathVariable UUID leadIdentifier,
            @PathVariable UUID contactIdentifier,
            @PathVariable UUID enquiryIdentifier) {
        Optional<SummaryResponse> summary = leadCreditBureauReadService.getSummaryResponseByEnquiryIdentifier(leadIdentifier, contactIdentifier, enquiryIdentifier);
        return summary.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("enquiry/{enquiryIdentifier}/status")
    public ResponseEntity<EnquiryStatusResponse> getEnquiryStatus(
            @PathVariable UUID leadIdentifier,
            @PathVariable UUID contactIdentifier,
            @PathVariable UUID enquiryIdentifier) {
        Optional<EnquiryStatusResponse> status = leadCreditBureauReadService.getEnquiryStatusByEnquiryIdentifier(leadIdentifier, contactIdentifier, enquiryIdentifier);
        return status.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/enquiry/{enquiryIdentifier}/consent-status")
    public ResponseEntity<EnquiryConsentStatusResponse> getConsentStatus(
            @PathVariable UUID leadIdentifier,
            @PathVariable UUID contactIdentifier,
            @PathVariable UUID enquiryIdentifier) {
        Optional<EnquiryConsentStatusResponse> consentStatus =
                leadCreditBureauReadService.getConsentStatusByEnquiryIdentifier(leadIdentifier, contactIdentifier, enquiryIdentifier);
        return consentStatus.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/enquiry/{enquiryIdentifier}/score-trends")
    public ResponseEntity<List<TrendsResponse>> getScoreTrends(
            @PathVariable UUID leadIdentifier,
            @PathVariable UUID contactIdentifier,
            @PathVariable UUID enquiryIdentifier) {
        List<TrendsResponse> scoreTrends = leadCreditBureauReadService.getScoreTrendsByEnquiryIdentifier(leadIdentifier, contactIdentifier, enquiryIdentifier);
        return ResponseEntity.ok(scoreTrends);
    }

    @GetMapping("/enquiry/{enquiryIdentifier}/demographic-variations")
    public ResponseEntity<List<DemographicVariationResponse>> getDemographicVariations(
            @PathVariable UUID leadIdentifier,
            @PathVariable UUID contactIdentifier,
            @PathVariable UUID enquiryIdentifier) {
        List<DemographicVariationResponse> demographicVariations = leadCreditBureauReadService.getDemographicVariationsByEnquiryIdentifier(leadIdentifier, contactIdentifier, enquiryIdentifier);
        return ResponseEntity.ok(demographicVariations);
    }
}


