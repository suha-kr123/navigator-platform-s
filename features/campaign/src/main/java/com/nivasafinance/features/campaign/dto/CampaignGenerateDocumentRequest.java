package com.nivasafinance.features.campaign.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CampaignGenerateDocumentRequest {
    private Map<String, Object> parameters;
}


