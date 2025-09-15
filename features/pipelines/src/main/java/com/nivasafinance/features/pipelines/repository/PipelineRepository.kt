package com.nivasafinance.features.pipelines.repository

import com.nivasafinance.features.pipelines.entity.Pipeline
import org.javers.spring.annotation.JaversSpringDataAuditable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
@JaversSpringDataAuditable
interface PipelineRepository : JpaRepository<Pipeline, UUID> {
}
