package com.nivasafinance.features.whatsapp.entity;

import com.nivasafinance.common.audit.AuditableEntity;
import com.nivasafinance.features.whatsapp.enums.WhatsappCreatedSource;
import com.nivasafinance.features.whatsapp.enums.WhatsappMessageType;
import com.nivasafinance.features.whatsapp.enums.WhatsappSentBy;
import com.nivasafinance.features.whatsapp.enums.WhatsappStatus;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "n_whatsapp_log")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class WhatsappLog extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(name = "identifier", nullable = false, unique = true, updatable = false)
    private UUID identifier = UUID.randomUUID();

    @Column(name = "provider", nullable = false, length = 100)
    private String provider;

    @Column(name = "provider_message_id", length = 255)
    private String providerMessageId;

    @Column(name = "provider_conversation_id", length = 255)
    private String providerConversationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sent_by", length = 50)
    private WhatsappSentBy sentBy;

    @Column(name = "phone", nullable = false, length = 50)
    private String phone;

    @Column(name = "from_channel_id", length = 100)
    private String fromChannelId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private WhatsappStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", length = 50)
    private WhatsappMessageType messageType;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "message_data", columnDefinition = "jsonb")
    private MessageData messageData;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "template_details", columnDefinition = "jsonb")
    private TemplateDetails templateDetails;

    @Enumerated(EnumType.STRING)
    @Column(name = "created_source", length = 50)
    private WhatsappCreatedSource createdSource;

    @Column(name = "message_time")
    private LocalDateTime messageTime;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TemplateDetails {
        private String templateId;
        private String templateName;
        private String category;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MessageData {
        private FileDetails fileDetails;
        private String text;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class FileDetails {
            private String caption;
            private String fileUrl;
            private String filetype;
            private String fileName;
        }
    }
}
