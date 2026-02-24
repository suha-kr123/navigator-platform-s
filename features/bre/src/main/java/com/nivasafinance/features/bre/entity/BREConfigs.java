package com.nivasafinance.features.bre.entity;

import com.nivasafinance.common.audit.IdentifiableEntity;
import com.nivasafinance.features.bre.enums.BREProvider;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "n_bre_configs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BREConfigs extends IdentifiableEntity {

    @Column(name = "uname", nullable = false, unique = true)
    private String uname;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config", nullable = false, columnDefinition = "jsonb")
    private Configs configs;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Configs {
        private Long dataProviderId;
        private BREProvider provider;
        private GoRulesProviderDetails goRulesProviderDetails;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoRulesProviderDetails {
        private Long ruleJsonFileId; //link to n_document#id
    }
}