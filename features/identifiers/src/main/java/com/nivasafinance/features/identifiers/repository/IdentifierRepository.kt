package com.nivasafinance.features.identifiers.repository

import com.nivasafinance.features.identifiers.entity.Identifier
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface IdentifierRepository : JpaRepository<Identifier, UUID> {

    fun findByEntityIdAndEntityType(entityId: UUID, entityType: String): List<Identifier>

    fun findByEntityIdAndEntityTypeAndType(entityId: UUID, entityType: String, type: String): List<Identifier>

    fun findByEntityIdAndEntityTypeAndIsPrimary(entityId: UUID, entityType: String, isPrimary: Boolean): List<Identifier>

    fun findByIdentifierAndType(identifier: String, type: String): List<Identifier>

    fun existsByEntityIdAndEntityTypeAndType(entityId: UUID, entityType: String, type: String): Boolean

    fun existsByEntityIdAndEntityTypeAndTypeAndIsPrimary(entityId: UUID, entityType: String, type: String, isPrimary: Boolean): Boolean

    @Query(
        "SELECT i FROM Identifier i WHERE i.entityType = :entityType AND i.type = :type AND i.identifier = :identifier"
    )
    fun findByEntityTypeAndTypeAndIdentifier(
        @Param("entityType") entityType: String,
        @Param("type") type: String,
        @Param("identifier") identifier: String
    ): List<Identifier>
}
