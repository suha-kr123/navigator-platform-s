package com.nivasafinance.features.otp.core.service;

import com.nivasafinance.features.otp.core.dto.OtpSendCommand;
import com.nivasafinance.features.otp.core.dto.OtpSendResult;
import com.nivasafinance.features.otp.core.dto.OtpVerifyCommand;
import com.nivasafinance.features.otp.core.dto.OtpVerifyResult;

public interface OtpCoreService {
    OtpSendResult sendOtp(OtpSendCommand command);
    OtpVerifyResult verifyOtp(OtpVerifyCommand command);
}
