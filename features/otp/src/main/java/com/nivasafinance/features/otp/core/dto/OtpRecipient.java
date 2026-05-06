package com.nivasafinance.features.otp.core.dto;

import com.nivasafinance.features.otp.core.enums.OtpChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpRecipient {
    private OtpChannel channel;
    private String destination;
}
