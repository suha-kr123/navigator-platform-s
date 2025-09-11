package com.nivasafinance.features.stages.repository

import com.nivasafinance.features.stages.entity.TransitionDefinition
import org.javers.spring.annotation.JaversSpringDataAuditable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
@JaversSpringDataAuditable
@Suppress("MaximumLineLength", "ArgumentListWrapping", "MaxLineLength")
interface TransitionDefinitionRepository : JpaRepository<TransitionDefinition, UUID> {

    fun findByPipeline(pipeline: String): List<TransitionDefinition>

    @Query("SELECT td FROM TransitionDefinition td WHERE td.pipeline = :pipeline AND td.fromStage = :fromStage")
    fun findByPipelineAndFromStage(
        @Param("pipeline") pipeline: String,
        @Param("fromStage") fromStage: String
    ): List<TransitionDefinition>

    @Query("SELECT td FROM TransitionDefinition td WHERE td.pipeline = :pipeline AND td.toStage = :toStage")
    fun findByPipelineAndToStage(
        @Param("pipeline") pipeline: String,
        @Param("toStage") toStage: String
    ): List<TransitionDefinition>

    @Query(
        "SELECT td FROM TransitionDefinition td WHERE td.pipeline = :pipeline AND td.fromStage = :fromStage AND td.toStage = :toStage"
    )
    fun findByPipelineAndFromStageAndToStage(
        @Param("pipeline") pipeline: String,
        @Param("fromStage") fromStage: String,
        @Param("toStage") toStage: String
    ): TransitionDefinition?

    @Query("SELECT td FROM TransitionDefinition td WHERE td.fromStage = :fromStage")
    fun findByFromStage(@Param("fromStage") fromStage: String): List<TransitionDefinition>

    @Query("SELECT td FROM TransitionDefinition td WHERE td.toStage = :toStage")
    fun findByToStage(@Param("toStage") toStage: String): List<TransitionDefinition>
}
