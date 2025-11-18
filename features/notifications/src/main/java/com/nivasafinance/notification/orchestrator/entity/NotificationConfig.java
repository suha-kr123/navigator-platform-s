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
import java.util.UUID;

/**
 * Defines notification behaviour, templates, recipients, and data providers.
 */
@Entity
@Table(name = "n_notification_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NotificationConfig extends IdentifiableEntity {

    @Column(name = "identifier", nullable = false, unique = true)
    private UUID identifier;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> config;

    @Column(name = "status", nullable = false, length = 50)
    private String status;
}


