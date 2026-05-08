package com.nivasafinance.features.creditbureau.entity;

import com.nivasafinance.common.audit.IdentifiableEntity;
import com.nivasafinance.features.creditbureau.enums.CreditBureauEnquiryStatus;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "n_cb_enquiry")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class CreditBureauEnquiry extends IdentifiableEntity {

    @Column(name = "identifier", nullable = false, unique = true)
    private UUID identifier = UUID.randomUUID();

    @Column(name = "report_id", length = 255)
    private String reportId;

    @Column(name = "status", length = 50)
    @Enumerated(EnumType.STRING)
    private CreditBureauEnquiryStatus status;

    @Column(name = "consent_id")
    private Long consentId;

    @Column(name = "provider", length = 100)
    private String provider;

    @Column(name = "error", columnDefinition = "TEXT")
    private String error;

    @Column(name = "request_json", columnDefinition = "TEXT")
    private String requestJson;

    @Column(name = "response_json", columnDefinition = "TEXT")
    private String responseJson;

    @Column(name = "report_document_identifier")
    private UUID reportDocumentIdentifier;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "report_details", columnDefinition = "jsonb")
    private ReportDetails reportDetails;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReportDetails {
        private LocalDateTime reportReceivedTime;
    }
}

