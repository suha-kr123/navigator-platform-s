package com.nivasafinance.externals.customer.lead.service.impl;

import com.nivasafinance.externals.customer.lead.service.LeadOtpExternalService;
import com.nivasafinance.features.leadotp.dto.CreateLeadSendOtpRequest;
import com.nivasafinance.features.leadotp.dto.CreateLeadSendOtpResponse;
import com.nivasafinance.features.leadotp.dto.CreateLeadVerifyOtpRequest;
import com.nivasafinance.features.leadotp.dto.CreateLeadVerifyOtpResponse;
import com.nivasafinance.features.leadotp.dto.LeadContactSendOtpResponse;
import com.nivasafinance.features.leadotp.dto.LeadContactVerifyOtpRequest;
import com.nivasafinance.features.leadotp.dto.LeadContactVerifyOtpResponse;
import com.nivasafinance.features.leadotp.service.LeadOtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeadOtpExternalServiceImpl implements LeadOtpExternalService {

    private final LeadOtpService leadOtpService;

    @Override
    public LeadContactSendOtpResponse sendVerifyLeadForCbOtp(UUID leadIdentifier, UUID contactIdentifier) {
        return leadOtpService.sendVerifyLeadForCbOtp(leadIdentifier, contactIdentifier);
    }

    @Override
    public LeadContactVerifyOtpResponse verifyLeadForCbOtp(
            UUID leadIdentifier,
            UUID contactIdentifier,
            LeadContactVerifyOtpRequest request) {
        return leadOtpService.verifyLeadForCbOtp(leadIdentifier, contactIdentifier, request);
    }

    @Override
    public CreateLeadSendOtpResponse sendCreateLeadOtp(CreateLeadSendOtpRequest request) {
        return leadOtpService.sendCreateLeadOtp(request);
    }

    @Override
    public CreateLeadVerifyOtpResponse verifyCreateLeadOtp(CreateLeadVerifyOtpRequest request) {
        return leadOtpService.verifyCreateLeadOtp(request);
    }
}
