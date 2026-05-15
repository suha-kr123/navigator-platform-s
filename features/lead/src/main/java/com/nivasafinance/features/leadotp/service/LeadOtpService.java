package com.nivasafinance.features.leadotp.service;

import com.nivasafinance.features.leadotp.dto.CreateLeadSendOtpRequest;
import com.nivasafinance.features.leadotp.dto.CreateLeadSendOtpResponse;
import com.nivasafinance.features.leadotp.dto.CreateLeadVerifyOtpRequest;
import com.nivasafinance.features.leadotp.dto.CreateLeadVerifyOtpResponse;
import com.nivasafinance.features.leadotp.dto.LeadContactSendOtpResponse;
import com.nivasafinance.features.leadotp.dto.LeadContactVerifyOtpRequest;
import com.nivasafinance.features.leadotp.dto.LeadContactVerifyOtpResponse;

import java.util.UUID;

public interface LeadOtpService {
    LeadContactSendOtpResponse sendVerifyLeadForCbOtp(UUID leadIdentifier, UUID contactIdentifier);

    LeadContactVerifyOtpResponse verifyLeadForCbOtp(
            UUID leadIdentifier,
            UUID contactIdentifier,
            LeadContactVerifyOtpRequest request);

    CreateLeadSendOtpResponse sendCreateLeadOtp(CreateLeadSendOtpRequest request);

    CreateLeadVerifyOtpResponse verifyCreateLeadOtp(CreateLeadVerifyOtpRequest request);
}
