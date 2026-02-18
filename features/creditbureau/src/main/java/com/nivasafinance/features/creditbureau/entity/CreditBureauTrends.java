package com.nivasafinance.features.creditbureau.entity;

import com.nivasafinance.common.audit.IdentifiableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "n_cb_trend")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class CreditBureauTrends extends IdentifiableEntity {

    @Column(name = "identifier", nullable = false, unique = true)
    @Builder.Default
    private UUID identifier = UUID.randomUUID();

    @Column(name = "enquiry_id", nullable = false)
    private Long enquiryId;

    @Column(name = "trend_name", length = 255)
    private String trendName;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "score_value")
    private Integer scoreValue;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}