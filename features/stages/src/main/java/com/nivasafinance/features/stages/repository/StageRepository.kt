package com.nivasafinance.features.stages.repository

import com.nivasafinance.features.stages.entity.Stage
import org.javers.spring.annotation.JaversSpringDataAuditable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
@JaversSpringDataAuditable
interface StageRepository : JpaRepository<Stage, UUID> {

    fun findByKey(key: String): Stage?

    fun findByPipelineKey(pipelineKey: String): List<Stage>

    fun existsByKey(key: String): Boolean

    @Query("SELECT s FROM Stage s WHERE s.pipelineKey = :pipelineKey AND s.name = :name")
    fun findByPipelineKeyAndName(
        @Param("pipelineKey") pipelineKey: String,
        @Param("name") name: String
    ): List<Stage>

    @Query("SELECT s FROM Stage s WHERE s.pipelineKey = :pipelineKey ORDER BY s.name")
    fun findByPipelineKeyOrderByName(@Param("pipelineKey") pipelineKey: String): List<Stage>
}
