package com.nivasafinance.features.pipelines.entity

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
@Table(name = "pipelines")
@NoArg
@Suppress("LongParameterList")
data class Pipeline(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "key", nullable = false, unique = true)
    val key: String,

    @Column(name = "description", columnDefinition = "TEXT")
    val description: String? = null
    
) : AuditableEntity()
