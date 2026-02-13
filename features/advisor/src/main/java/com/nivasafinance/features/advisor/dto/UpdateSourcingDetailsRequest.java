package com.nivasafinance.features.advisor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating sourcing details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSourcingDetailsRequest {
    private String sourcingChannel;
    private String marketingSource;
    private String sourceId;
    private String sourceUrl;
    private String campaignId;
    private String referralCode;
}
