package com.nivasafinance.features.otp.core.dto;

import com.nivasafinance.features.otp.core.enums.OtpReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerifyCommand {
    private OtpReference reference;
    private String otp;
    private OtpScope scope;
}
