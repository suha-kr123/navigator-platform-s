package com.nivasafinance.features.leadotp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadContactVerifyOtpResponse {
    private boolean verified;
}
