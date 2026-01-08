package com.nivasafinance.features.campaign.entity;

import com.nivasafinance.common.audit.IdentifiableEntity;
import com.nivasafinance.features.campaign.enums.CampaignDocumentStatus;
import com.nivasafinance.features.campaign.enums.CampaignProviderDocumentStatus;
import com.nivasafinance.features.campaign.enums.CampaignStatus;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "n_campaign")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Campaign extends IdentifiableEntity {

    @Column(name = "identifier", nullable = false, unique = true)
    private UUID identifier;

    @Column(name = "config_id", nullable = false)
    private Long configId;

    @Column(name = "provider", length = 100)
    private String provider;

    @Column(name = "provider_id", length = 100)
    private String providerId;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "provider_details", columnDefinition = "jsonb")
    private ProviderDetails providerDetails;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private CampaignStatus status;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "document_details", columnDefinition = "jsonb")
    private DocumentDetails documentDetails;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "summary", columnDefinition = "jsonb")
    private Summary summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProviderDetails {
        private VoiceDetails voiceDetails;
        private String error;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VoiceDetails {
        private String callerId;
        private String appFlowId;
        private Integer noOfRetries;
        private Integer retryInterval;
        private Integer cpm;
        private String listId;
        private String documentUploadId;
        private CampaignProviderDocumentStatus documentStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentDetails {
        private CampaignDocumentStatus documentStatus;
        private Long documentId; // csv document id generated
        private String error;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private String reportUrl;
        private Long scheduled;
        private Long initialized;
        private Long completed;
        private Long failed;
        private Long inProgress;
    }
}