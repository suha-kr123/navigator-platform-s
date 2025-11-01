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

    @Column(name = "office_key")
    private String officeKey;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "contact_details", columnDefinition = "jsonb")
    private List<ContactDetails> contactDetails;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "applicant_details", columnDefinition = "jsonb")
    private ApplicantDetails applicantDetails;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "co_applicant_details", columnDefinition = "jsonb")
    private List<CoApplicantDetails> coApplicantDetails;

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
    private Map<String, String> preliminaryDetails;

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
    @Column(name = "checklist", columnDefinition = "jsonb")
    private Checklist checklist;

    // Nested data classes for JSONB fields

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContactDetails {
        private Long contactId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApplicantDetails {
        private Long applicantId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CoApplicantDetails {
        private Long applicantId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WorkflowDetails {
        private String workflowIdentifier;
        private String assignedTo;
        private String currentStage;
        private String currentOutcome;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DocumentDetail {
        private Long id;
        private String status;
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
        private String eligibleLoanAmount;
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
        private PropertyDetails propertyDetails;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PropertyDetails {
        private AddressData address;
        private GeoData geoData;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Checklist {
        private String type;
        private List<ChecklistItem> data;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChecklistItem {
        private Long id;
        private String tag;
        private String status;
    }
}
