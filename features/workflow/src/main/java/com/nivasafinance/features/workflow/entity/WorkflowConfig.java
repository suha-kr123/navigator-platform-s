package com.nivasafinance.features.workflow.entity;

import com.nivasafinance.common.audit.AuditableEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "n_workflow_config")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class WorkflowConfig extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "workflow_config_key", nullable = false, unique = true, length = 100)
    private String workflowConfigKey;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "workflow_config_details", columnDefinition = "jsonb")
    private Map<String, Object> workflowConfigDetails;

    @Column(name = "is_active")
    private Boolean isActive;
}

