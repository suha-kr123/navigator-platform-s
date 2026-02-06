package com.nivasafinance.features.creditbureau.entity;

import com.nivasafinance.common.audit.IdentifiableEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "n_cb_summary")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class CreditBureauSummary extends IdentifiableEntity {

    @Column(name = "identifier", nullable = false, unique = true)
    @Builder.Default
    private UUID identifier = UUID.randomUUID();

    @Column(name = "enquiry_id", nullable = false)
    private Long enquiryId;

    @Column(name = "credit_score")
    private Integer creditScore;

    @Column(name = "score_version", length = 50)
    private String scoreVersion;

    @Column(name = "score_name", length = 255)
    private String scoreName;

    @Column(name = "total_accounts")
    private Integer totalAccounts;

    @Column(name = "active_accounts")
    private Integer activeAccounts;

    @Column(name = "overdue_accounts")
    private Integer overdueAccounts;

    @Column(name = "closed_accounts")
    private Integer closedAccounts;

    @Column(name = "secured_accounts")
    private Integer securedAccounts;

    @Column(name = "unsecured_accounts")
    private Integer unsecuredAccounts;

    @Column(name = "untagged_accounts")
    private Integer untaggedAccounts;

    @Column(name = "total_current_balance", precision = 18, scale = 2)
    private BigDecimal totalCurrentBalance;

    @Column(name = "current_balance_secured", precision = 18, scale = 2)
    private BigDecimal currentBalanceSecured;

    @Column(name = "current_balance_unsecured", precision = 18, scale = 2)
    private BigDecimal currentBalanceUnsecured;

    @Column(name = "total_overdue_amount", precision = 18, scale = 2)
    private BigDecimal totalOverdueAmount;

    @Column(name = "total_sanctioned_amount", precision = 18, scale = 2)
    private BigDecimal totalSanctionedAmount;

    @Column(name = "total_disbursed_amount", precision = 18, scale = 2)
    private BigDecimal totalDisbursedAmount;

    @Column(name = "no_of_own_mfis")
    private Integer noOfOwnMfis;

    @Column(name = "no_of_other_mfis")
    private Integer noOfOtherMfis;

    @Column(name = "total_own_current_balance", precision = 18, scale = 2)
    private BigDecimal totalOwnCurrentBalance;

    @Column(name = "total_own_installment_amount", precision = 18, scale = 2)
    private BigDecimal totalOwnInstallmentAmount;

    @Column(name = "total_own_disbursed_amount", precision = 18, scale = 2)
    private BigDecimal totalOwnDisbursedAmount;

    @Column(name = "total_other_installment_amount", precision = 18, scale = 2)
    private BigDecimal totalOtherInstallmentAmount;

    @Column(name = "total_other_disbursed_amount", precision = 18, scale = 2)
    private BigDecimal totalOtherDisbursedAmount;

    @Column(name = "total_other_overdue_amount", precision = 18, scale = 2)
    private BigDecimal totalOtherOverdueAmount;

    @Column(name = "max_worst_delinquency")
    private Integer maxWorstDelinquency;

    @Column(name = "account_count")
    private Integer accountCount;

    @Type(JsonType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "score_factor_details", columnDefinition = "jsonb")
    private List<ScoreFactor> scoreFactorDetails;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScoreFactor {
        private String factorType;
        private String factorDescription;
    }
}
