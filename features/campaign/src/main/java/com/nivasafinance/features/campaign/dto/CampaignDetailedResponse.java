package com.nivasafinance.features.campaign.dto;

import com.nivasafinance.features.campaign.entity.Campaign;
import com.nivasafinance.features.campaign.enums.CampaignDocumentStatus;
import com.nivasafinance.features.campaign.enums.CampaignStatus;
import com.nivasafinance.features.document.dto.DocumentResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignDetailedResponse {
    private Long campaignId;
    private String name;
    private String identifier;
    private CampaignConfigDetailedResponse configs;
    private String provider;
    private String providerId;
    private CampaignStatus status;
    private CampaignDocumentStatus documentStatus;
    private DocumentResponse document;
    private Campaign.Summary summary;
    private Campaign.VoiceDetails voiceDetails;

    public static CampaignDetailedResponse from(
            Campaign entity,
            CampaignConfigDetailedResponse configResponse,
            DocumentResponse document) {
        if (entity == null) {
            return null;
        }

        Campaign.DocumentDetails documentDetails = entity.getDocumentDetails();
        Campaign.ProviderDetails providerDetails = entity.getProviderDetails();

        return CampaignDetailedResponse.builder()
                .campaignId(entity.getId())
                .identifier(entity.getIdentifier().toString())
                .name(entity.getName())
                .configs(configResponse)
                .provider(entity.getProvider())
                .providerId(entity.getProviderId())
                .status(entity.getStatus())
                .documentStatus(documentDetails != null ? documentDetails.getDocumentStatus() : null)
                .document(document)
                .summary(entity.getSummary())
                .voiceDetails(providerDetails != null ? providerDetails.getVoiceDetails() : null)
                .build();
    }
}
