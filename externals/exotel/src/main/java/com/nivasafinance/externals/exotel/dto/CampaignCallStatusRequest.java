package com.nivasafinance.externals.exotel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignCallStatusRequest {

    @NotBlank(message = "campaign_sid is required")
    @JsonProperty("campaign_sid")
    private String campaignSid;

    @NotBlank(message = "call_sid is required")
    @JsonProperty("call_sid")
    private String callSid;
}
