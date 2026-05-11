package com.nivasafinance.features.otp.core.config;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OtpRuntimeConfig {
    private Integer otpValidityInMins;
    private String otpGenerationMethod;
    @Builder.Default
    private List<OtpChannelConfig> otpChannels = new ArrayList<>();
}
