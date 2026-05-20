package com.nivasafinance.features.advisor.entity;

import com.nivasafinance.common.audit.AuditableEntity;
import com.nivasafinance.common.enums.SourcingChannel;
import com.nivasafinance.features.advisor.dto.AdvisorRemarks;
import com.nivasafinance.features.advisor.dto.BankDetails;
import com.nivasafinance.features.advisor.dto.OtherDetails;
import com.nivasafinance.features.advisor.dto.QualificationDetails;
import com.nivasafinance.features.advisor.dto.SegmentationDetails;
import com.nivasafinance.features.advisor.enums.AdvisorStatus;
import com.nivasafinance.common.enums.ReferredByType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "n_advisor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Advisor extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "identifier", nullable = false, unique = true)
    private UUID identifier;

    @Column(name = "username", length = 255)
    private String username;

    @Column(name = "status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private AdvisorStatus status;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "remarks", columnDefinition = "jsonb")
    private AdvisorRemarks remarks;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "bank_details", columnDefinition = "jsonb")
    private List<BankDetails> bankDetails;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "qualification_details", columnDefinition = "jsonb")
    private QualificationDetails qualificationDetails;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "other_details", columnDefinition = "jsonb")
    private OtherDetails otherDetails;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "segmentation_details", columnDefinition = "jsonb")
    private SegmentationDetails segmentationDetails;

    @Column(name = "source_channel_id")
    private Long sourceChannelId;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sourcing_history", columnDefinition = "jsonb")
    private List<SourcingEntry> sourcingHistory = new ArrayList<>();

    @Column(name = "referred_by_code", length = 100)
    private String referredByCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "referred_by_type", length = 50)
    private ReferredByType referredByType;

    @Column(name = "referred_by_identifier")
    private UUID referredByIdentifier;

    @Column(name = "office_key", length = 100)
    private String officeKey;

    @Column(name = "owner", length = 255)
    private String owner;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rejection_details", columnDefinition = "jsonb")
    private RejectionDetails rejectionDetails;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "notes", columnDefinition = "jsonb")
    private List<Long> notes;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "external_ids", columnDefinition = "jsonb")
    private Map<String, String> externalIds;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "call_logs", columnDefinition = "jsonb")
    private List<CallLogDetails> callLogDetails;

    @Column(name = "referral_code", length = 255, unique = true, nullable = false)
    private String referralCode;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RejectionDetails {
        private LocalDateTime rejectionDate;
        private String rejectedBy;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CallLogDetails {
        private Long callLogId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SourcingEntry {
        private SourcingChannel sourcingChannel;
        private String marketingSource;
        private String campaignId;
        private String sourceId;
        private String sourceUrl;
        private String googleClickId;
        private LocalDateTime capturedAt;
    }
}

