package com.nivasafinance.notification.orchestrator.entity;

import com.nivasafinance.common.audit.AuditableEntity;
import com.nivasafinance.notification.orchestrator.enums.WhatsAppMessageStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Tracks WhatsApp notifications sent to advisors.
 * Stores WATI response data and webhook updates for message status tracking.
 */
@Entity
@Table(name = "n_advisor_whatsapp_notification")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AdvisorWhatsAppNotification extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "advisor_identifier", nullable = false)
    private UUID advisorIdentifier;

    @Column(name = "receipt_id", nullable = false)
    private UUID receiptId;

    @Column(name = "notification_record_id", nullable = false)
    private UUID notificationRecordId;

    @Column(name = "template_id", length = 255)
    private String templateId;

    @Column(name = "template_name", length = 255)
    private String templateName;

    @Column(name = "local_message_id", length = 255, unique = true)
    private String localMessageId;

    @Column(name = "whatsapp_message_id", length = 500)
    private String whatsappMessageId;

    @Column(name = "wa_id", length = 50)
    private String waId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    @Builder.Default
    private WhatsAppMessageStatus status = WhatsAppMessageStatus.SENT;

    @Column(name = "is_replied")
    @Builder.Default
    private Boolean isReplied = false;

    @Column(name = "reply_text", columnDefinition = "TEXT")
    private String replyText;

    @Column(name = "reply_timestamp")
    private Instant replyTimestamp;

    @Column(name = "delivered_timestamp")
    private Instant deliveredTimestamp;

    @Column(name = "sent_timestamp")
    private Instant sentTimestamp;

    @Column(name = "conversation_id", length = 255)
    private String conversationId;

    @Column(name = "ticket_id", length = 255)
    private String ticketId;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "is_valid_whatsapp_number")
    private Boolean isValidWhatsAppNumber;
}

