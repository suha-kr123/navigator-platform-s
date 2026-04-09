package com.nivasafinance.features.master.pincode.entity;

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

@Entity
@Table(name = "n_master_pincode_valuation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PincodeValuationMaster extends IdentifiableEntity {

    @Column(name = "pincode", nullable = false, length = 6, unique = true)
    private String pincode;

    @Column(name = "tier", length = 50)
    private String tier;

    @Column(name = "form3_valuation")
    private BigDecimal form3Valuation;

    @Column(name = "valuation_11a")
    private BigDecimal valuation11a;

    @Column(name = "valuation_11b")
    private BigDecimal valuation11b;
}
