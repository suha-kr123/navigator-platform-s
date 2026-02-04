package com.nivasafinance.externals.exotel.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignCallbackRequest {
    
    @NotBlank(message = "campaign_sid is required")
    private String campaign_sid;
    private String status;
}
