package com.nivasafinance.features.stagedefinitions.repository

import com.nivasafinance.features.stagedefinitions.entity.StageDefinition
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface StageDefinitionRepository : JpaRepository<StageDefinition, UUID> {
}
