package com.nivasafinance.notification.orchestrator.entity;

import com.nivasafinance.common.audit.AuditableEntity;
import com.nivasafinance.notification.orchestrator.enums.AppNotificationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/** Tracks FCM send result per device (sent/failed, provider message id). */
@Entity
@Table(name = "n_app_notification")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AppNotification extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    @Column(name = "receipt_id", nullable = false)
    private UUID receiptId;

    @Column(name = "notification_record_id", nullable = false)
    private UUID notificationRecordId;

    @Column(name = "notification_token", length = 500)
    private String notificationToken;

    @Column(name = "template_id", length = 255)
    private String templateId;

    @Column(name = "template_name", length = 255)
    private String templateName;

    @Column(name = "provider_message_id", length = 500)
    private String providerMessageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    @Builder.Default
    private AppNotificationStatus status = AppNotificationStatus.SENT;

    @Column(name = "error_code", length = 100)
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "sent_timestamp")
    private Instant sentTimestamp;

    @Column(name = "opened_at")
    private Instant openedAt;
}
