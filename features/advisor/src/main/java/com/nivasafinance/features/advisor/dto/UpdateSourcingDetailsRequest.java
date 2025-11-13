package com.nivasafinance.features.advisor.dto;

import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "sourcingChannel is required")
    private String sourcingChannel;

    @NotBlank(message = "marketingSource is required")
    private String marketingSource;
    private String sourceId;
    private String campaignId;
    private String sourcedBy; 
}
