package com.nivasafinance.features.stage.entity;

import com.nivasafinance.common.audit.IdentifiableEntity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.util.Map;

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
    private Map<String, Object> stageConfig;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "assignee_roles", columnDefinition = "jsonb")
    private Map<String, Object> assigneeRoles;

    @Column(name = "sub_stages_code", length = 100)
    private String subStagesCode;

    @Column(name = "is_active")
    private Boolean isActive = true;
}

