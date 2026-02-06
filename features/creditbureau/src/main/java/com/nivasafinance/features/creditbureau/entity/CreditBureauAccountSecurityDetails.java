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
import java.util.UUID;

@Entity
@Table(name = "n_cb_account_security_detail")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class CreditBureauAccountSecurityDetails extends IdentifiableEntity {

    @Column(name = "identifier", nullable = false, unique = true)
    @Builder.Default
    private UUID identifier = UUID.randomUUID();

    @Column(name = "tradeline_id", nullable = false)
    private Long tradelineId;

    @Column(name = "security_type", length = 100)
    private String securityType;

    @Column(name = "owner_name", length = 200)
    private String ownerName;

    @Column(name = "security_valuation", precision = 18, scale = 2)
    private BigDecimal securityValuation;

    @Column(name = "date_of_valuation")
    private LocalDate dateOfValuation;

    @Column(name = "security_charge", length = 100)
    private String securityCharge;

    @Column(name = "property_address", columnDefinition = "TEXT")
    private String propertyAddress;

    @Column(name = "automobile_type", length = 100)
    private String automobileType;

    @Column(name = "year_of_manufacturing")
    private Integer yearOfManufacturing;

    @Column(name = "registration_number", length = 50)
    private String registrationNumber;

    @Column(name = "engine_number", length = 50)
    private String engineNumber;

    @Column(name = "chassis_number", length = 50)
    private String chassisNumber;
}
