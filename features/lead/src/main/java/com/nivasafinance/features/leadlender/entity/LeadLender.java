package com.nivasafinance.features.leadlender.entity;

import com.nivasafinance.common.audit.AuditableEntity;
import com.nivasafinance.features.leadlender.dto.ApprovedDetails;
import com.nivasafinance.features.leadlender.dto.LoginDetails;
import com.nivasafinance.features.leadlender.dto.RmDetails;
import com.nivasafinance.features.leadlender.enums.LeadLenderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "n_lead_lender")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeadLender extends AuditableEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lender_identifier", nullable = false, unique = true, updatable = false)
    private UUID lenderIdentifier;

    @Column(name = "lead_id", nullable = false)
    private Long leadId;

    @Column(name = "lender_key", nullable = false, length = 20)
    private String lenderKey;

    @Column(name = "status", nullable = false, length = 40)
    @Enumerated(EnumType.STRING)
    private LeadLenderStatus status;

    @Column(name = "lender_office_key", length = 20)
    private String lenderOfficeKey;

    @Column(name = "login_details", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private LoginDetails loginDetails;

    @Column(name = "rm_details", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private RmDetails rmDetails;

    @Column(name = "approved_details", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private ApprovedDetails approvedDetails;

    @Column(name = "stage", length = 100)
    private String stage;

    @Column(name = "remarks", length = 100)
    private String remarks;
}
