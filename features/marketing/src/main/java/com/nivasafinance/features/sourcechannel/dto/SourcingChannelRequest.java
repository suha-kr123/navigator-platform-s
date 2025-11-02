package com.nivasafinance.features.sourcechannel.dto;

import lombok.AllArgsConstructor;
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
    public static class MarketingDetails {
        private String sourceId;
    }
}

