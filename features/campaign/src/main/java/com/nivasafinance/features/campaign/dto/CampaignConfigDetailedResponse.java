package com.nivasafinance.features.campaign.dto;

import com.nivasafinance.features.campaign.entity.CampaignConfig;
import com.nivasafinance.features.campaign.enums.CampaignConfigStatus;
import com.nivasafinance.features.campaign.enums.CampaignType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignConfigDetailedResponse {
    private String identifier;
    private String name;
    private CampaignType type;
    private CampaignConfigStatus status;
    private CampaignConfig.VoiceConfigs voiceConfigs;
    private Long dataProviderId;
    private String fileType;

    public static CampaignConfigDetailedResponse from(CampaignConfig entity) {
        if (entity == null) {
            return null;
        }

        CampaignConfig.Configs configs = entity.getConfigs();
        return CampaignConfigDetailedResponse.builder()
                .identifier(entity.getIdentifier().toString())
                .name(entity.getName())
                .type(configs != null ? configs.getCampaignType() : null)
                .status(entity.getStatus())
                .voiceConfigs(configs != null ? configs.getVoiceConfigs() : null)
                .dataProviderId(configs != null ? configs.getDataProviderId() : null)
                .fileType(configs != null ? configs.getFileType().toString() : null)
                .build();
    }
}
