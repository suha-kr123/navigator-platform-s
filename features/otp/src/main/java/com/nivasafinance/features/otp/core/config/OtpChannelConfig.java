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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OtpChannelConfig {
    private OtpChannel channelName;
    @Builder.Default
    private List<OtpChannelRuntimeConfig> channelConfig = new ArrayList<>();

    public String resolveTemplateName() {
        return channelConfig.stream()
                .map(OtpChannelRuntimeConfig::getTemplateName)
                .filter(templateName -> templateName != null && !templateName.isBlank())
                .findFirst()
                .orElse(null);
    }
}
