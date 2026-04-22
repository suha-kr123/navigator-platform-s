package com.nivasafinance.features.leadqueues.entity;

import java.util.List;
import java.time.LocalDateTime;
import com.nivasafinance.common.audit.AuditableEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import io.hypersistence.utils.hibernate.type.json.JsonType;

@Entity
@Table(name = "n_queue_config")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class QueueConfig extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "queue_name", nullable = false, length = 255, unique = true)
    private String queueName;

    @Column(name = "description", length = 500)
    private String description;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data_providers", nullable = false)
    private DataProviderDetails dataProvider;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "user_ids", nullable = false, columnDefinition = "jsonb")
    private List<Long> userIds;

    @Column(name = "lock_duration", nullable = false)
    private Integer lockDuration = 10;

    /**
     * Minimum time between reorders, in <strong>seconds</strong> (new inserts default to 5).
     */
    @Column(name = "reorder_time", nullable = false)
    private Integer reorderTime = 5;

    @Column(name = "last_reorder_time", nullable = true)
    private LocalDateTime lastReorderTime;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataProviderDetails {
        private String dataProviderName;
    }

}
