package com.nivasafinance.features.lead.entity;

import com.nivasafinance.common.audit.AuditableEntity;
import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.GeoData;
import com.nivasafinance.common.enums.TenureType;
import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.enums.LeadSubStatus;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "n_lead")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class Lead extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lead_identifier", nullable = false, unique = true)
    private UUID leadIdentifier;

    @Column(name = "requested_amount", precision = 18, scale = 2)
    private BigDecimal requestedAmount;

    @Column(name = "office_key", nullable = false)
    private String officeKey;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "contacts", columnDefinition = "jsonb")
    private List<Long> contacts;

    @Column(name = "applicant")
    private Long applicant;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "co_applicants")
    private List<Long> coApplicants;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "workflow_details", columnDefinition = "jsonb")
    private WorkflowDetails workflowDetails;

    @Column(name = "purpose", length = 255)
    private String purpose;

    @Column(name = "product_code", length = 100)
    private String productCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 100)
    private LeadStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "substatus", length = 100)
    private LeadSubStatus substatus;

    @Column(name = "owner", length = 255)
    private String owner;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preliminary_details", columnDefinition = "jsonb")
    private PreliminaryDetails preliminaryDetails;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "document_details", columnDefinition = "jsonb")
    private List<DocumentDetail> documentDetails;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "notes", columnDefinition = "jsonb")
    private List<Long> notes;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "credit_rating_details", columnDefinition = "jsonb")
    private CreditRatingDetails creditRatingDetails;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "disbursement_details", columnDefinition = "jsonb")
    private DisbursementDetails disbursementDetails;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "reasons", columnDefinition = "jsonb")
    private ReasonDetails reasons;

    @Column(name = "sourcing_channel_id")
    private Long sourcingChannelId;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "proposed_details", columnDefinition = "jsonb")
    private ProposedDetails proposedDetails;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "other_details", columnDefinition = "jsonb")
    private OtherDetails otherDetails;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "onhold_details", columnDefinition = "jsonb")
    private OnHoldDetails onHoldDetails;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rejection_details", columnDefinition = "jsonb")
    private RejectionDetails rejectionDetails;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "withdrawn_details", columnDefinition = "jsonb")
    private WithdrawnDetails withdrawnDetails;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dropoff_details", columnDefinition = "jsonb")
    private DropoffDetails dropoffDetails;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "call_logs", columnDefinition = "jsonb")
    private List<CallLogDetails> callLogDetails;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "external_ids", columnDefinition = "jsonb")
    private Map<String, String> externalIds;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "income_obligation_details", columnDefinition = "jsonb")
    private IncomeObligationDetails incomeObligationDetails;

    // Nested data classes for JSONB fields

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WorkflowDetails {
        private String workflowConfigKey;
        private CurrentStageDetails currentStageDetails;
        private LastStageDetails lastStageDetails;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CurrentStageDetails {
        private String stageKey;    
        private String subStageKey;
        private String assignedTo;
        private LocalDateTime assignedAt;
        private LocalDateTime enteredAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LastStageDetails {
        private String stageKey;
        private String subStageKey;
        private String assignedTo;
        private LocalDateTime assignedAt;
        private LocalDateTime enteredAt;
        private LocalDateTime exitedAt;
        private String movedBy;
        private String remarks;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DocumentDetail {
        private Long id;
        private List<String> tag;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreditRatingDetails {
        private String underwriter;
        private String occupationProfile;
        private String roofProfile;
        private String ltv;
        private String foir;
        private String monthlyFamilyIncome;
        private String propertyDocumentType;
        private BigDecimal eligibleLoanAmount;
        private String location;
        private String bureauRating;
        private String customerProfiles;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DisbursementDetails {
        private BigDecimal disbursedAmount;
        private BigDecimal roi;
        private Integer tenureValue;
        private TenureType tenureType;
        private LocalDate disbursedDate;
        private BigDecimal processingFees;
        private List<Tranche> tranches;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Tranche {
        private UUID identifier;
        private BigDecimal amount;
        private LocalDate date;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReasonDetails {
        private String reject;
        private String withdrawn;
        private String onhold;
        private String dropoff;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProposedDetails {
        private BigDecimal proposedLoanAmount;
        private BigDecimal roi;
        private Integer tenureValue;
        private TenureType tenureType;
        private BigDecimal emi;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OtherDetails {
        private Long primaryContactId;
        private Long lastCallId;
        private PropertyDetails propertyDetails;
        private String priority;
        private LocalTime preferredCallStartTime;
        private LocalTime preferredCallEndTime;
        private Long noOfCampaignCalls;
        private String currentCustomerFormStep;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PropertyDetails {
        private AddressData address;
        private GeoData geoData;
        private String propertyType;
        private String propertyConstructionStage;
        private PropertyMeasurementDetails propertyMeasurementDetails;
        private DocumentChecklist documentChecklist;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class PropertyMeasurementDetails {
            private String buildUpArea;
            private String siteArea;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DocumentChecklist {
        private String aKhata;
        private String bKhata;
        private String saleDeed;
        private String propertyTax;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OnHoldDetails {
        private LocalDateTime onHoldMovementDate;
        private String onHoldBy;
        private LocalDate holdFollowUpDate;
    }

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
    public static class WithdrawnDetails {
        private LocalDateTime withdrawnDate;
        private String withdrawnBy;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DropoffDetails {
        private LocalDateTime dropoffDate;
        private String dropoffBy;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CallLogDetails {
        private Long callLogId;
        private Long contactId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PreliminaryDetails {
        private Boolean isWhatsAppDIYFormCompleted;
        private Map<String, String> whatsAppDIYForm;
        private BigDecimal monthlyFamilyIncome;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IncomeObligationDetails {
        private List<IncomeDetails> incomeDetails;
        private ObligationDetails obligationDetails;
        private BigDecimal monthlyFamilyIncome;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IncomeDetails {
        private String incomeSource;
        private BigDecimal amount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ObligationDetails {
        private BigDecimal existingEmi;
    }

}
