package com.nivasafinance.features.task.entity;

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

import java.time.LocalDateTime;
import java.util.Map;

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

    @Column(name = "task_config_key", nullable = false, length = 100)
    private String taskConfigKey;

    @Column(name = "assigned_to", length = 255)
    private String assignedTo;

    @Column(name = "assigned_to_role", length = 100)
    private String assignedToRole;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    @Column(name = "outcome", length = 100)
    private String outcome;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "outcome_details", columnDefinition = "jsonb")
    private Map<String, Object> outcomeDetails;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "task_details", columnDefinition = "jsonb")
    private Map<String, Object> taskDetails;
}

