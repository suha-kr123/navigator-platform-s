package com.nivasafinance.features.call.entity;

import com.nivasafinance.common.audit.AuditableEntity;
import com.nivasafinance.features.call.enums.CallDirection;
import com.nivasafinance.features.call.enums.CallProvider;
import com.nivasafinance.features.call.enums.CallSource;
import com.nivasafinance.features.call.enums.CallStatus;
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
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "n_call_log")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class CallLog extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "identifier", nullable = false, unique = true)
    private UUID identifier = UUID.randomUUID();

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 100)
    private CallProvider provider;

    @Column(name = "provider_id", nullable = false, length = 255)
    private String providerId;

    @Column(name = "caller_id", length = 255)
    private String callerId;

    @Column(name = "from_number", length = 50)
    private String fromNumber;

    @Column(name = "to_number", length = 50)
    private String toNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", length = 50)
    private CallDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 50)
    private CallSource source;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private CallStatus status;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "recording_details", columnDefinition = "jsonb")
    private RecordingDetails recordingDetails;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "completion_details", columnDefinition = "jsonb")
    private CompletionDetails completionDetails;

    @Column(name = "campaign_id")
    private Long campaignId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecordingDetails {
        private String url;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompletionDetails {
        private Long duration;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private List<CompletionLeg> legs;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompletionLeg {
        private String duration;
        private CallStatus status;
    }
}