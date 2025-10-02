package com.nivasafinance.features.lead.entity

import annotations.NoArg
import audit.AuditableEntity
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "leads")
@NoArg
@Suppress("LongParameterList")
data class Lead(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "requested_amount", columnDefinition = "jsonb", nullable = true)
    var requestedAmountRange: Map<String, BigDecimal>?,

    @Column(name = "purpose", length = 40, nullable = true)
    var purpose: String?,

    @Column(name = "product_code", nullable = true)
    var productCode: String?,

    @Column(name = "pipeline_key", nullable = true)
    var pipelineKey: String?,

    @Column(name = "current_stage", nullable = true)
    var currentStage: String?,

    @Column(name = "sourcing_channel")
    var sourcingChannel: String?,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preliminary_information", columnDefinition = "jsonb")
    var preliminaryInformation: Map<String, Any>? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ext_data", columnDefinition = "jsonb")
    var extData: Map<String, Any>? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "task_data", columnDefinition = "jsonb", nullable = true)
    var taskData: List<TaskData>? = null,

    @Column(name = "address_id", nullable = true)
    var addressId: UUID? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "stage_ids", columnDefinition = "jsonb", nullable = true)
    var stageIds: List<UUID>? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "document_ids", columnDefinition = "jsonb", nullable = true)
    var documentIds: List<LeadDocumentData>? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "person_data", columnDefinition = "jsonb", nullable = true)
    var personData: List<PersonData>? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "note_ids", columnDefinition = "jsonb", nullable = true)
    var notes: List<LeadNotesData>? = null

) : AuditableEntity()

@JsonIgnoreProperties(ignoreUnknown = true)
data class TaskData(
    @JsonProperty("taskId")
    val taskId: UUID,
)

data class LeadDocumentData(
    val documentId: UUID,
    val taskId: UUID? = null,
    val createdDate: LocalDateTime
)

data class LeadNotesData(
    val noteId: UUID,
    val taskId: UUID? = null,
    val createdDate: LocalDateTime
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PersonData(
    @JsonProperty("personId")
    val personId: UUID,
    @JsonProperty("applicantType")
    val applicantType: String,
    @JsonProperty("relationshipToPrimary")
    val relationshipToPrimary: String
)
