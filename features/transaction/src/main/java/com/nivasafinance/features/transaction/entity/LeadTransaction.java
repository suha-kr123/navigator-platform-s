package com.nivasafinance.features.transaction.entity;

import com.nivasafinance.features.transaction.enums.DomainType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "n_lead_transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeadTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "identifier", nullable = false, unique = true)
    private UUID identifier;

    @Column(name = "lead_id", nullable = false)
    private Long leadId;

    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "domain_type", nullable = false, length = 100)
    private DomainType domainType;

    @Column(name = "referral_code")
    private String referralCode;

    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Version
    @Column(name = "version")
    private Long version = 0L;
}
