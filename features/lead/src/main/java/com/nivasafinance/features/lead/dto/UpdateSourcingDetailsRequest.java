package com.nivasafinance.features.lead.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSourcingDetailsRequest {
    private String sourcingChannel;
    private String marketingSource;
    private String sourceId;
    private String sourceUrl;
    private String campaignId;
    private String referredByCode;
}
