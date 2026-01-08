package com.nivasafinance.features.campaign.dto;

import com.nivasafinance.features.campaign.entity.Campaign;
import com.nivasafinance.features.campaign.enums.CampaignDocumentStatus;
import com.nivasafinance.features.campaign.enums.CampaignStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignResponse {
    String name;
    String identifier;
    CampaignStatus status;
    CampaignDocumentStatus documentStatus;
    Campaign.Summary summary;

    public static CampaignResponse from(Campaign entity) {
        if (entity == null) {
            return null;
        }

        Campaign.DocumentDetails documentDetails = entity.getDocumentDetails();
        return CampaignResponse.builder()
                .identifier(entity.getIdentifier().toString())
                .name(entity.getName())
                .status(entity.getStatus())
                .documentStatus(documentDetails != null ? documentDetails.getDocumentStatus() : null)
                .summary(entity.getSummary())
                .build();
    }
}
