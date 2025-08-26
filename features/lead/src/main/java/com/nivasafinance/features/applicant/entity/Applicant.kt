package com.nivasafinance.features.applicant.entity

import audit.AuditableEntity
import com.nivasafinance.features.applicant.enum.ApplicantStatus
import com.nivasafinance.features.applicant.enum.ApplicantType
import com.nivasafinance.features.applicant.enum.RelationshipToPrimary
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "lead_applicant")
class Applicant(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(name = "lead_id", nullable = false)
    var leadId: UUID,

    @Column(name = "person_id", nullable = false)
    var personId: UUID,

    @Column(name = "applicant_type", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    var applicantType: ApplicantType = ApplicantType.PRIMARY,

    @Column(name = "relationship_to_primary", length = 30, nullable = false)
    @Enumerated(EnumType.STRING)
    var relationshipToPrimary: RelationshipToPrimary = RelationshipToPrimary.SELF,

    @Column(name = "status", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    var status: ApplicantStatus = ApplicantStatus.NEEDS_TO_BE_REVIEWED,

) : AuditableEntity()
