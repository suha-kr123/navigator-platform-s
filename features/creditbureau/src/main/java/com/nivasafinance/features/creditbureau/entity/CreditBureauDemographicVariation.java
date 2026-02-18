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

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "n_cb_demographic_variation")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class CreditBureauDemographicVariation extends IdentifiableEntity {

    @Column(name = "identifier", nullable = false, unique = true)
    @Builder.Default
    private UUID identifier = UUID.randomUUID();

    @Column(name = "enquiry_id", nullable = false)
    private Long enquiryId;

    @Column(name = "variation_type", length = 50)
    private String variationType;

    @Column(name = "variation_value", columnDefinition = "TEXT")
    private String variationValue;

    @Column(name = "reported_date")
    private LocalDate reportedDate;

    @Column(name = "first_reported_date")
    private LocalDate firstReportedDate;

    @Column(name = "loan_type_associated", columnDefinition = "TEXT")
    private String loanTypeAssociated;

    @Column(name = "source_indicator", length = 50)
    private String sourceIndicator;
}
