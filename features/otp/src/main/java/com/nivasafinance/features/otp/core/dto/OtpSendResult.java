package com.nivasafinance.features.otp.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpSendResult {
    private Long oneTimeTokenId;
    private String reference;
    private Integer validityInMins;
}
