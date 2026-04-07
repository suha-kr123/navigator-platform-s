package com.nivasafinance.features.stage.entity;

import com.nivasafinance.common.audit.IdentifiableEntity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "n_stage_config")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class StageConfig extends IdentifiableEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "key", nullable = false, unique = true, length = 100)
    private String key;

    @Column(name = "description", length = 500)
    private String description;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "stage_config", columnDefinition = "jsonb")
    private StageConfigDetails stageConfig;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "assignee_roles", columnDefinition = "jsonb")
    private AssigneeRoles assigneeRoles;

    @Column(name = "sub_stages_code", length = 100)
    private String subStagesCode;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StageConfigDetails {
        private List<PossibleNextStage> possibleNextStages;
        private String externalDisplayName;
        private Integer externalOrder;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PossibleNextStage {
        private String stageKey;
        private List<String> allowedRoles;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssigneeRoles {
        private List<String> roles;
    }
}

