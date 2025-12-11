package com.nivasafinance.features.task.entity;

import com.nivasafinance.common.audit.AuditableEntity;
import com.nivasafinance.common.enums.EntityType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "n_tasks")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class Task extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "task_identifier", nullable = false, unique = true, updatable = false, columnDefinition = "UUID")
    @Setter(AccessLevel.NONE)
    private UUID taskIdentifier;

    @Column(name = "task_config_key", nullable = false, length = 100)
    private String taskConfigKey;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "assigned_to", length = 255)
    private String assignedTo;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    @Column(name = "outcome", length = 100)
    private String outcome;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "outcome_details", columnDefinition = "jsonb")
    private OutcomeDetails outcomeDetails;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "task_details", columnDefinition = "jsonb")
    private TaskDetails taskDetails;

    @jakarta.persistence.PrePersist
    void prePersist() {
        if (!com.nivasafinance.common.utils.ValidationUtils.isNonNull(taskIdentifier)) {
            taskIdentifier = UUID.randomUUID();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OutcomeDetails {
        private String remarks;
        private LocalDateTime completedAt;
        private String completedBy;
        private String rescheduleReasonCodeValueKey;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaskDetails {
        private UUID entityId;
        private EntityType entityType;
        private PreferredCallWindow preferredCallWindow;
        private String creatorRemarks;
        private Integer iterationCount;
        private UUID rescheduledFromTaskIdentifier;
        private String rescheduleReasonCodeValueKey;
        private String rescheduledFromTaskRemarks;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class PreferredCallWindow {
            private LocalDateTime start;
            private LocalDateTime end;
        }
    }
}

