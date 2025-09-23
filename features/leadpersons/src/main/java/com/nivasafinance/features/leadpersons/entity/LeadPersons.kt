package com.nivasafinance.features.leadpersons.entity

import annotations.NoArg
import audit.AuditableEntity    
import jakarta.persistence.*
import java.util.UUID
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(name = "lead_persons")
@NoArg
data class LeadPersons(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(name = "lead_id", nullable = false)
    var leadId: UUID? = null,

    @Column(name = "person_id", nullable = false)
    var personId: UUID? = null,

    @Column(name = "is_applicant", nullable = false)
    var isApplicant: Boolean? = null,

    @Column(name = "applicant_type", nullable = false)
    var applicantType: String? = null,

    @Column(name = "relationship_to_primary", nullable = false)
    var relationshipToPrimary: String? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "jsonb")
    var tags: List<String>? = null,

    @Column(name = "verification_status", nullable = false)
    var verificationStatus: String? = null,

    @Column(name = "verification_notes")
    var verificationNotes: String? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ext_data", columnDefinition = "jsonb")
    var extData: Map<String, Any>? = null

) : AuditableEntity()
