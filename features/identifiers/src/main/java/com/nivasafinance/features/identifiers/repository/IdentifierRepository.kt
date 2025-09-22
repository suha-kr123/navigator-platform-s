package com.nivasafinance.features.identifiers.repository

import com.nivasafinance.features.identifiers.entity.Identifier
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface IdentifierRepository : JpaRepository<Identifier, UUID> {
    
    fun findAllByEntityTypeAndEntityId(entityType: String, entityId: UUID): List<Identifier>
}
