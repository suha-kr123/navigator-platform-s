package com.nivasafinance.features.lead.entity;

import com.nivasafinance.common.audit.AuditableEntity;
import com.nivasafinance.features.lead.enums.ApplicantPersonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "n_applicant")
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class Applicant extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "identifier", nullable = false, unique = true)
    private UUID identifier;

    @Column(name = "person_id", nullable = false)
    private Long personId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 100)
    private ApplicantPersonType type;
}
