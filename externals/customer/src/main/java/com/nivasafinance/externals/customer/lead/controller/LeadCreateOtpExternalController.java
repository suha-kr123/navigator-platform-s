package com.nivasafinance.externals.customer.lead.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.externals.customer.lead.service.LeadOtpExternalService;
import com.nivasafinance.features.leadotp.dto.CreateLeadSendOtpRequest;
import com.nivasafinance.features.leadotp.dto.CreateLeadSendOtpResponse;
import com.nivasafinance.features.leadotp.dto.CreateLeadVerifyOtpRequest;
import com.nivasafinance.features.leadotp.dto.CreateLeadVerifyOtpResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.OPEN_API_V1 + "/leads/create")
@RequiredArgsConstructor
public class LeadCreateOtpExternalController {

    private final LeadOtpExternalService leadOtpExternalService;

    @PostMapping("/send/otp")
    public ResponseEntity<CreateLeadSendOtpResponse> sendCreateLeadOtp(
            @RequestBody @Valid CreateLeadSendOtpRequest request) {
        return ResponseEntity.ok(leadOtpExternalService.sendCreateLeadOtp(request));
    }

    @PostMapping("/verify/otp")
    public ResponseEntity<CreateLeadVerifyOtpResponse> verifyCreateLeadOtp(
            @RequestBody @Valid CreateLeadVerifyOtpRequest request) {
        return ResponseEntity.ok(leadOtpExternalService.verifyCreateLeadOtp(request));
    }
}
