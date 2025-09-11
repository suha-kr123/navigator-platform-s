package com.nivasafinance.features.pipelines.repository

import com.nivasafinance.features.pipelines.entity.Pipeline
import com.nivasafinance.features.pipelines.enum.EntityType
import org.javers.spring.annotation.JaversSpringDataAuditable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
@JaversSpringDataAuditable
interface PipelineRepository : JpaRepository<Pipeline, UUID> {

    fun findByKey(key: String): Pipeline?

    fun findByEntityType(entityType: EntityType): List<Pipeline>

    fun existsByKey(key: String): Boolean

    @Query("SELECT p FROM Pipeline p WHERE p.entityType = :entityType AND p.name = :name")
    fun findByEntityTypeAndName(
        @Param("entityType") entityType: EntityType,
        @Param("name") name: String
    ): List<Pipeline>
}
