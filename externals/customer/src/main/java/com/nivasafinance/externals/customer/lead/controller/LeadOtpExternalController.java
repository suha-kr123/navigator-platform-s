package com.nivasafinance.externals.customer.lead.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.externals.customer.lead.service.LeadOtpExternalService;
import com.nivasafinance.features.leadotp.dto.LeadContactSendOtpResponse;
import com.nivasafinance.features.leadotp.dto.LeadContactVerifyOtpRequest;
import com.nivasafinance.features.leadotp.dto.LeadContactVerifyOtpResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.OPEN_API_V1 + "/leads/{leadIdentifier}/contacts/{contactIdentifier}")
@RequiredArgsConstructor
public class LeadOtpExternalController {

    private final LeadOtpExternalService leadOtpExternalService;

    @PostMapping("/send/otp")
    public ResponseEntity<LeadContactSendOtpResponse> sendVerifyLeadForCbOtp(
            @PathVariable UUID leadIdentifier,
            @PathVariable UUID contactIdentifier) {
        return ResponseEntity.ok(leadOtpExternalService.sendVerifyLeadForCbOtp(leadIdentifier, contactIdentifier));
    }

    @PostMapping("/verify/otp")
    public ResponseEntity<LeadContactVerifyOtpResponse> verifyLeadForCbOtp(
            @PathVariable UUID leadIdentifier,
            @PathVariable UUID contactIdentifier,
            @RequestBody @Valid LeadContactVerifyOtpRequest request) {
        return ResponseEntity.ok(leadOtpExternalService.verifyLeadForCbOtp(leadIdentifier, contactIdentifier, request));
    }
}
