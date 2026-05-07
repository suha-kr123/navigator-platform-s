package com.nivasafinance.features.sourcechannel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SourcingChannelRequest {
    private String sourcingChannel;
    private String marketingSource;
    private MarketingDetails marketingDetails;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MarketingDetails {
        private String sourceId;
        private String sourceUrl;
        private String campaignId;
        private String referredByCode; // referral code
        private String googleClickId;
    }
}

