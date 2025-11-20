package com.nivasafinance.notification.orchestrator.entity;

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

import java.util.Map;

/**
 * Stores template configuration for notification channels.
 */
@Entity
@Table(name = "n_notification_template")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NotificationTemplate extends IdentifiableEntity {

    @Column(name = "identifier", nullable = false, unique = true, length = 255)
    private String identifier;

    @Column(name = "channel", length = 50)
    private String channel;

    @Column(name = "mode", length = 50)
    private String mode;

    @Column(name = "detail", columnDefinition = "TEXT")
    private String detail;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "variables", columnDefinition = "jsonb")
    private Map<String, Object> variables;

    @Column(name = "status", nullable = false, length = 50)
    private String status;
}


