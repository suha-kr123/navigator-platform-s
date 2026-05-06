package com.nivasafinance.features.otp.core.config;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nivasafinance.features.otp.core.enums.OtpChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OtpRuntimeConfig {
    private Integer otpValidityInMins;
    private String otpGenerationMethod;
    private Integer maxResendAttempts;

    @Builder.Default
    private List<OtpChannelConfig> otpChannels = new ArrayList<>();

    public Optional<OtpChannelConfig> getChannelConfig(OtpChannel channel) {
        return Optional.ofNullable(otpChannels)
                .orElse(List.of())
                .stream()
                .filter(config -> config.getChannelName() == channel)
                .findFirst();
    }
}
