package com.nivasafinance.features.pipelines.entity

import com.nivasafinance.features.pipelines.enum.EntityType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Table
import org.javers.core.metamodel.annotation.Id
import org.javers.spring.annotation.JaversSpringDataAuditable
import java.util.UUID

@Entity
@Table(name = "pipelines")
@JaversSpringDataAuditable
@Suppress("ImportOrdering")
data class Pipeline(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: UUID? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    val entityType: EntityType,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "key", nullable = false, unique = true)
    val key: String,

    @Column(name = "description", columnDefinition = "TEXT")
    val description: String? = null
)
