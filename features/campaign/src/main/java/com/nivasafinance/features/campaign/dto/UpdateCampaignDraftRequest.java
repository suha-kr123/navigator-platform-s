package com.nivasafinance.features.campaign.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCampaignDraftRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private VoiceDetails voiceDetails;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoiceDetails {
        private Integer cpm;
        private LocalDateTime scheduledAt;
    }
}
