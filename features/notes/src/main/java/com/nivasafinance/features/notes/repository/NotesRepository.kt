package com.nivasafinance.features.notes.repository

import com.nivasafinance.features.notes.entity.Notes
import com.nivasafinance.features.notes.enum.EntityType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface NotesRepository : JpaRepository<Notes, UUID> {
    
    fun findAllByEntityTypeAndEntityId(entityType: EntityType, entityId: UUID): List<Notes>
    
    fun findAllByParentId(parentId: UUID): List<Notes>
}
