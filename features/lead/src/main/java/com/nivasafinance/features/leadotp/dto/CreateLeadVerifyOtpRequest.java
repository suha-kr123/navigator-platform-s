package com.nivasafinance.features.leadotp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeadVerifyOtpRequest {
    @NotNull(message = "oneTimeTokenId is required")
    private Long oneTimeTokenId;

    @NotBlank(message = "OTP is required")
    private String otp;
}
