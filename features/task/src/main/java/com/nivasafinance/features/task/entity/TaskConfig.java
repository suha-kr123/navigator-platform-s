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
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "n_task_config")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class TaskConfig extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_config_key", nullable = false, unique = true, length = 100)
    private String taskConfigKey;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "task_config_details", columnDefinition = "jsonb")
    private TaskConfigDetails taskConfigDetails;

    @Column(name = "is_active")
    private Boolean isActive;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaskConfigDetails {
        private String allowedOutcomesCodeValueKey;
        private Boolean isRescheduledAllowed;
        private String rescheduleReasonsCodeValueKey;
        private List<String> allowedRoles;
        private String dueDateLogicExpression;
        private Boolean isAdhocTaskAllowed;
    }
}

