package com.nivasafinance.notification.executor.entity;

import com.nivasafinance.common.audit.AuditableEntity;
import com.nivasafinance.common.enums.NotificationStatus;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import java.util.Map;
import java.util.UUID;

/**
 * Represents a concrete notification to be executed for a recipient.
 */
@Entity
@Table(name = "n_notification_receipt")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NotificationReceipt extends AuditableEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "notification_record_id", nullable = false)
    private UUID notificationRecordId;

    @Column(name = "mode", length = 50)
    private String mode;

    @Column(name = "recipient_contact", length = 255)
    private String recipientContact;

    @Column(name = "channel_type", length = 50)
    private String channelType;

    @Column(name = "template_identifier", length = 255)
    private String templateIdentifier;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "message_payload", columnDefinition = "jsonb")
    private Map<String, Object> messagePayload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.INITIATED;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details", columnDefinition = "jsonb")
    private Map<String, Object> details;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "schedules", columnDefinition = "jsonb")
    private List<Map<String, Object>> schedules;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "remarks", columnDefinition = "jsonb")
    private Map<String, Object> remarks;
}


