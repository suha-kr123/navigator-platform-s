package com.nivasafinance.features.leadtasks.entity;

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

@Entity
@Table(name = "n_lead_tasks")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class LeadTask extends IdentifiableEntity {

    @Column(name = "lead_id", nullable = false)
    private Long leadId;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "task_details", columnDefinition = "jsonb")
    private TaskDetails taskDetails;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaskDetails {
        private String stageKey;
    }
}
