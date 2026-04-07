package com.nivasafinance.features.workflow.entity;

import com.nivasafinance.common.audit.AuditableEntity;
import com.nivasafinance.common.enums.EntityType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "n_pending_workflow_actions")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class PendingWorkflowAction extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "action_identifier", nullable = false, unique = true, updatable = false, columnDefinition = "UUID")
    @Setter(AccessLevel.NONE)
    private UUID actionIdentifier;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "entity_identifier", nullable = false, columnDefinition = "UUID")
    private UUID entityIdentifier;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 50)
    private EntityType entityType;

    @Column(name = "current_stage_key", nullable = false, length = 100)
    private String currentStageKey;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "source_task_identifier", nullable = false, columnDefinition = "UUID")
    private UUID sourceTaskIdentifier;

    @Column(name = "source_task_config_key", nullable = false, length = 100)
    private String sourceTaskConfigKey;

    @Column(name = "source_outcome", nullable = false, length = 100)
    private String sourceOutcome;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "action_details", nullable = false, columnDefinition = "jsonb")
    private ActionDetails actionDetails;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "executed_by", length = 255)
    private String executedBy;

    @jakarta.persistence.PrePersist
    void prePersist() {
        if (actionIdentifier == null) {
            actionIdentifier = UUID.randomUUID();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActionDetails {
        private String type;
        private String taskConfigKey;
        private String targetStageKey;
        private String targetSubStageKey;
        private String assignTo;
        private String outcome;
        private LocalDateTime dueDate;
    }

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_EXECUTED = "EXECUTED";
    public static final String STATUS_CANCELLED = "CANCELLED";
}
