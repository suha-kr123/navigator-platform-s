package com.nivasafinance.services.voice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoiceCampaignResponse {
    private String campaignId;
    private String providerKey;
    private VoiceCampaignStatus status;
    private String reportUrl;
    private Summary summary;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Summary {
        private Long scheduled;
        private Long initialized;
        private Long completed;
        private Long failed;
        private Long inProgress;
    }
}
