package com.nivasafinance.features.campaign.entity;

import com.nivasafinance.common.audit.IdentifiableEntity;
import com.nivasafinance.features.campaign.enums.CampaignConfigStatus;
import com.nivasafinance.features.campaign.enums.CampaignType;
import com.nivasafinance.redash.dto.FileType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "n_campaign_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CampaignConfig extends IdentifiableEntity {

    @Column(name = "identifier", nullable = false, unique = true)
    private UUID identifier;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config", nullable = false, columnDefinition = "jsonb")
    private Configs configs;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private CampaignConfigStatus status;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Configs {
        private Long dataProviderId;
        private FileType fileType;
        private CampaignType campaignType;
        private VoiceConfigs voiceConfigs;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoiceConfigs {
        private String callerId;
        private String appFlowId;
        private String appFlowName;
        private Integer defaultNoOfRetries;
        private Integer defaultRetryInterval;
        private Integer defaultCpm;
    }
}