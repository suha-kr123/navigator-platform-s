package com.nivasafinance.features.creditbureau.entity;

import com.nivasafinance.common.audit.IdentifiableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "n_cb_customer_tradeline")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class CreditBureauTradeline extends IdentifiableEntity {

    @Column(name = "identifier", nullable = false, unique = true)
    @Builder.Default
    private UUID identifier = UUID.randomUUID();

    @Column(name = "enquiry_id", nullable = false)
    private Long enquiryId;

    @Column(name = "account_number", length = 100)
    private String accountNumber;

    @Column(name = "credit_grantor", length = 200)
    private String creditGrantor;

    @Column(name = "credit_grantor_group", length = 50)
    private String creditGrantorGroup;

    @Column(name = "credit_grantor_type", length = 10)
    private String creditGrantorType;

    @Column(name = "account_type", length = 100)
    private String accountType;

    @Column(name = "account_status", length = 50)
    private String accountStatus;

    @Column(name = "reported_date")
    private LocalDate reportedDate;

    @Column(name = "closed_date")
    private LocalDate closedDate;

    @Column(name = "ownership_type", length = 50)
    private String ownershipType;

    @Column(name = "disbursed_amount", precision = 18, scale = 2)
    private BigDecimal disbursedAmount;

    @Column(name = "disbursed_date")
    private LocalDate disbursedDate;

    @Column(name = "current_balance", precision = 18, scale = 2)
    private BigDecimal currentBalance;

    @Column(name = "credit_limit", precision = 18, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "cash_limit", precision = 18, scale = 2)
    private BigDecimal cashLimit;

    @Column(name = "overdue_amount", precision = 18, scale = 2)
    private BigDecimal overdueAmount;

    @Column(name = "installment_amount", length = 50)
    private String installmentAmount;

    @Column(name = "installment_frequency", length = 100)
    private String installmentFrequency;

    @Column(name = "original_term")
    private Integer originalTerm;

    @Column(name = "term_to_maturity")
    private Integer termToMaturity;

    @Column(name = "repayment_tenure", length = 50)
    private String repaymentTenure;

    @Column(name = "interest_rate", length = 10)
    private String interestRate;

    @Column(name = "last_payment_date")
    private LocalDateTime lastPaymentDate;

    @Column(name = "last_paid_amount", precision = 18, scale = 2)
    private BigDecimal lastPaidAmount;

    @Column(name = "actual_payment", precision = 18, scale = 2)
    private BigDecimal actualPayment;

    @Column(name = "write_off_amount", precision = 18, scale = 2)
    private BigDecimal writeOffAmount;

    @Column(name = "principal_write_off_amount", precision = 18, scale = 2)
    private BigDecimal principalWriteOffAmount;

    @Column(name = "settlement_amount", precision = 18, scale = 2)
    private BigDecimal settlementAmount;

    @Column(name = "write_off_date")
    private LocalDate writeOffDate;

    @Column(name = "security_status", length = 100)
    private String securityStatus;

    @Column(name = "obligation", precision = 18, scale = 2)
    private BigDecimal obligation;

    @Column(name = "account_remarks", columnDefinition = "TEXT")
    private String accountRemarks;

    @Column(name = "account_in_dispute")
    private Boolean accountInDispute;

    @Column(name = "suit_filed_wilful_default_status", length = 100)
    private String suitFiledWilfulDefaultStatus;

    @Column(name = "written_off_settled_status", length = 100)
    private String writtenOffSettledStatus;

    @Column(name = "suit_filed_date")
    private LocalDate suitFiledDate;

    @Column(name = "occupation", length = 100)
    private String occupation;

    @Column(name = "income_frequency", length = 50)
    private String incomeFrequency;

    @Column(name = "income_amount", precision = 18, scale = 2)
    private BigDecimal incomeAmount;
}
