package com.nivasafinance.features.campaign.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignDraftRequest {
    @NotNull(message = "Config Identifier is required")
    private UUID configIdentifier;
    @NotBlank(message = "Name is required")
    private String name;

    private VoiceDetails voiceDetails;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoiceDetails {
        private Integer cpm;
    }
}
