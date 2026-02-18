package com.nivasafinance.features.consent.entity;

import com.nivasafinance.common.audit.IdentifiableEntity;
import com.nivasafinance.features.consent.enums.ConsentStatus;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "n_consent")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class Consent extends IdentifiableEntity {

    @Column(name = "identifier", nullable = false, unique = true)
    private UUID identifier = UUID.randomUUID();

    @Column(name = "status", length = 50)
    @Enumerated(EnumType.STRING)
    private ConsentStatus status;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "consent_sent_details", columnDefinition = "jsonb")
    private ConsentSentDetails consentSentDetails;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "consent_received_details", columnDefinition = "jsonb")
    private ConsentReceivedDetails consentReceivedDetails;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "consent_withdrawn_details", columnDefinition = "jsonb")
    private ConsentWithdrawnDetails consentWithdrawnDetails;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsentSentDetails {
        private LocalDateTime consentSentTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsentReceivedDetails {
        private LocalDateTime consentReceivedTime;
        private String auditId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsentWithdrawnDetails {
        private LocalDateTime consentWithdrawalRequestedTime;
        private String auditId;
    }
}
