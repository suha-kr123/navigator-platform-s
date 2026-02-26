package com.nivasafinance.features.lead.controller;

import com.nivasafinance.common.annotations.RequirePermission;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.creditbureau.dto.*;
import com.nivasafinance.features.lead.dto.EnquiryConsentStatusResponse;
import com.nivasafinance.features.lead.dto.EnquiryDetailsResponse;
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
@RequestMapping(ApiConstants.V1 + "/leads/{leadIdentifier}/contacts/{contactIdentifier}")
@AllArgsConstructor
public class LeadCreditBureauController {

    private final LeadCreditBureauWriteService leadCreditBureauWriteService;
    private final LeadCreditBureauReadService leadCreditBureauReadService;

    @PostMapping("/enquiry/initiate")
    @RequirePermission(permissionName = "CREATE_LEAD_CREDIT_DETAILS")
    public ResponseEntity<InitiateCbEnquiryResponse> initiateEnquiry(
            @PathVariable UUID leadIdentifier,
            @PathVariable UUID contactIdentifier) {
        InitiateCbEnquiryResponse response = leadCreditBureauWriteService.initiateEnquiry(leadIdentifier, contactIdentifier);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/enquiry")
    @RequirePermission(permissionName = "READ_LEAD_CREDIT_DETAILS")
    public ResponseEntity<EnquiryDetailsResponse> getEnquiryDetails(
            @PathVariable UUID leadIdentifier,
            @PathVariable UUID contactIdentifier) {
        return leadCreditBureauReadService.getEnquiryDetailsForContact(leadIdentifier, contactIdentifier)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/enquiry/{enquiryIdentifier}/customer-enquiry")
    @RequirePermission(permissionName = "READ_LEAD_CREDIT_DETAILS")
    public ResponseEntity<List<CustomerEnquiryResponse>> getCustomerEnquiry(
            @PathVariable UUID leadIdentifier,
            @PathVariable UUID contactIdentifier,
            @PathVariable UUID enquiryIdentifier) {
        List<CustomerEnquiryResponse> customerEnquiries = leadCreditBureauReadService.getCustomerEnquiryByEnquiryIdentifier(leadIdentifier, contactIdentifier, enquiryIdentifier);
        return ResponseEntity.ok(customerEnquiries);
    }

    @GetMapping("/enquiry/{enquiryIdentifier}/summary")
    @RequirePermission(permissionName = "READ_LEAD_CREDIT_DETAILS")
    public ResponseEntity<SummaryResponse> getSummary(
            @PathVariable UUID leadIdentifier,
            @PathVariable UUID contactIdentifier,
            @PathVariable UUID enquiryIdentifier) {
        Optional<SummaryResponse> summary = leadCreditBureauReadService.getSummaryResponseByEnquiryIdentifier(leadIdentifier, contactIdentifier, enquiryIdentifier);
        return summary.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("enquiry/{enquiryIdentifier}/status")
    @RequirePermission(permissionName = "READ_LEAD_CREDIT_DETAILS")
    public ResponseEntity<EnquiryStatusResponse> getEnquiryStatus(
            @PathVariable UUID leadIdentifier,
            @PathVariable UUID contactIdentifier,
            @PathVariable UUID enquiryIdentifier) {
        Optional<EnquiryStatusResponse> status = leadCreditBureauReadService.getEnquiryStatusByEnquiryIdentifier(leadIdentifier, contactIdentifier, enquiryIdentifier);
        return status.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/enquiry/{enquiryIdentifier}/consent-status")
    @RequirePermission(permissionName = "READ_LEAD_CREDIT_DETAILS")
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
    @RequirePermission(permissionName = "READ_LEAD_CREDIT_DETAILS")
    public ResponseEntity<List<TrendsResponse>> getScoreTrends(
            @PathVariable UUID leadIdentifier,
            @PathVariable UUID contactIdentifier,
            @PathVariable UUID enquiryIdentifier) {
        List<TrendsResponse> scoreTrends = leadCreditBureauReadService.getScoreTrendsByEnquiryIdentifier(leadIdentifier, contactIdentifier, enquiryIdentifier);
        return ResponseEntity.ok(scoreTrends);
    }

    @GetMapping("/enquiry/{enquiryIdentifier}/demographic-variations")
    @RequirePermission(permissionName = "READ_LEAD_CREDIT_DETAILS")
    public ResponseEntity<List<DemographicVariationResponse>> getDemographicVariations(
            @PathVariable UUID leadIdentifier,
            @PathVariable UUID contactIdentifier,
            @PathVariable UUID enquiryIdentifier) {
        List<DemographicVariationResponse> demographicVariations = leadCreditBureauReadService.getDemographicVariationsByEnquiryIdentifier(leadIdentifier, contactIdentifier, enquiryIdentifier);
        return ResponseEntity.ok(demographicVariations);
    }

    @PostMapping("/enquiry/{enquiryIdentifier}/cb-report/regenerate")
    @RequirePermission(permissionName = "CREATE_LEAD_DOCUMENTS")
    public ResponseEntity<Void> regenerateCbReport(
            @PathVariable UUID leadIdentifier,
            @PathVariable UUID contactIdentifier,
            @PathVariable UUID enquiryIdentifier) {
        leadCreditBureauWriteService.regenerateCbReport(leadIdentifier, contactIdentifier, enquiryIdentifier);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}


