package com.nivasafinance.features.creditbureau.entity;

import com.nivasafinance.common.audit.IdentifiableEntity;
import com.nivasafinance.features.creditbureau.enums.CbAttributeCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "n_cb_attribute")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class CreditBureauAttribute extends IdentifiableEntity {

    @Column(name = "enquiry_id", nullable = false)
    private Long enquiryId;

    @Column(name = "category", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private CbAttributeCategory category;

    @Column(name = "attr_name", nullable = false, length = 100)
    private String attrName;

    @Column(name = "attr_value", length = 100)
    private String attrValue;
}
