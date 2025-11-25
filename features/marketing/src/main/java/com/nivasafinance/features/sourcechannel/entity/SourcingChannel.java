package com.nivasafinance.features.sourcechannel.entity;

import com.nivasafinance.common.audit.AuditableEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "n_sourcing_channel_details")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class SourcingChannel extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sourcing_identifier", nullable = false, unique = true)
    private UUID sourcingIdentifier;

    @Column(name = "sourcing_channel_name", length = 255)
    private String sourcingChannel;

    @Column(name = "marketing_source", length = 255)
    private String marketingSource;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "marketing_details", columnDefinition = "jsonb")
    private MarketingDetails marketingDetails;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MarketingDetails {
        private String sourceId;
        private String campaignId;
        private String sourcedBy;
        private String sourceUrl;
    }
}
