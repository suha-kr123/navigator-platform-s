package com.nivasafinance.features.otp.core.service;

import com.nivasafinance.features.otp.core.dto.OtpRecipient;
import com.nivasafinance.features.otp.core.enums.OtpChannel;
import com.nivasafinance.features.otp.core.enums.OtpReference;

public interface OtpDeliveryService {
    OtpChannel getChannel();
    void send(OtpRecipient recipient, String otp, Integer validityInMins, OtpReference reference, String templateName);
}
