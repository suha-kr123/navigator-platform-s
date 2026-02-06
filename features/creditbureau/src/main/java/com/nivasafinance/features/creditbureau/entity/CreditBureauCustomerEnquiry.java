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
@Table(name = "n_cb_customer_enquiry")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class CreditBureauCustomerEnquiry extends IdentifiableEntity {

    @Column(name = "identifier", nullable = false, unique = true)
    @Builder.Default
    private UUID identifier = UUID.randomUUID();

    @Column(name = "enquiry_id", nullable = false)
    private Long enquiryId;

    @Column(name = "lender_name", length = 200)
    private String lenderName;

    @Column(name = "inquiry_date")
    private LocalDate inquiryDate;

    @Column(name = "ownership_type", length = 50)
    private String ownershipType;

    @Column(name = "credit_inquiry_purpose_type", length = 100)
    private String creditInquiryPurposeType;

    @Column(name = "inquiry_amount", precision = 18, scale = 2)
    private BigDecimal inquiryAmount;
}
