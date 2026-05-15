package com.nivasafinance.features.leadotp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeadVerifyOtpResponse {
    private boolean verified;
    private UUID leadIdentifier;
}
