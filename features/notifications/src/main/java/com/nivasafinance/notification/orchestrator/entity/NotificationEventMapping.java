package com.nivasafinance.notification.orchestrator.entity;

import com.nivasafinance.common.audit.IdentifiableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Maps business events to notification configurations.
 */
@Entity
@Table(name = "n_notification_event_mapping")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NotificationEventMapping extends IdentifiableEntity {

    @Column(name = "event", nullable = false, length = 100)
    private String event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notification_config_id", nullable = false)
    private NotificationConfig notificationConfig;

    @Column(name = "status", nullable = false, length = 50)
    private String status;
}


