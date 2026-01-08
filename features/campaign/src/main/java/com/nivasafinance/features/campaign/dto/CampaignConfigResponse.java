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
public class CampaignConfigResponse {
    private String identifier;
    private String name;
    private CampaignType type;
    private CampaignConfigStatus status;

    public static CampaignConfigResponse from(CampaignConfig entity) {
        if (entity == null) {
            return null;
        }

        CampaignConfig.Configs configs = entity.getConfigs();
        return CampaignConfigResponse.builder()
                .identifier(entity.getIdentifier().toString())
                .name(entity.getName())
                .type(configs != null ? configs.getCampaignType() : null)
                .status(entity.getStatus())
                .build();
    }
}
