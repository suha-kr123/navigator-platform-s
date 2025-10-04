package com.nivasafinance.features.notes.entity

import com.nivasafinance.common.annotations.NoArg
import com.nivasafinance.common.audit.AuditableEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "notes")
@NoArg
data class Notes(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "title", nullable = false)
    var title: String,

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    var content: String

) : AuditableEntity()
