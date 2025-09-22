package com.nivasafinance.features.notes.service

import base.model.PaginatedResponse
import base.model.PaginationInfo
import base.model.PaginationRequest
import com.nivasafinance.features.notes.dto.NotesRequest
import com.nivasafinance.features.notes.dto.NotesResponse
import com.nivasafinance.features.notes.dto.NotesUpdateRequest
import com.nivasafinance.features.notes.entity.Notes
import com.nivasafinance.features.notes.enum.EntityType
import com.nivasafinance.features.notes.exception.NotesExceptionFactory
import com.nivasafinance.features.notes.repository.NotesRepositoryWrapper
import org.springframework.context.MessageSource
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.util.*

@Service
class NotesServiceImpl(
    private val notesRepositoryWrapper: NotesRepositoryWrapper,
    private val messageSource: MessageSource
) : NotesService {

    private fun validateEntityType(entityType: String) {
        try {
            EntityType.valueOf(entityType.uppercase())
        } catch (e: IllegalArgumentException) {
            throw NotesExceptionFactory.unsupportedEntityType(entityType, messageSource)
        }
    }

    override fun createNotesByEntity(entityType: String, entityId: UUID, notesRequest: NotesRequest): NotesResponse {
        validateEntityType(entityType)
        val notes = Notes(
            entityType = entityType,
            entityId = entityId,
            title = notesRequest.title,
            content = notesRequest.content
        )
        val savedNotes = notesRepositoryWrapper.saveWithException(notes)
        return toNotesResponse(savedNotes)
    }

    override fun updateNotesByEntity(entityType: String, entityId: UUID, notesId: UUID, notesUpdateRequest: NotesUpdateRequest): NotesResponse {
        validateEntityType(entityType)
        val existingNotes = notesRepositoryWrapper.findByIdWithException(notesId)
        val updatedNotes = existingNotes.copy(
            title = notesUpdateRequest.title,
            content = notesUpdateRequest.content
        )
        val savedNotes = notesRepositoryWrapper.saveWithException(updatedNotes)
        return toNotesResponse(savedNotes)
    }
    
    override fun deleteNotesByEntity(entityType: String, entityId: UUID, notesId: UUID) {
        validateEntityType(entityType)
        notesRepositoryWrapper.deleteByIdWithException(notesId)
    }

    override fun getNotesByEntity(entityType: String, entityId: UUID, paginationRequest: PaginationRequest): PaginatedResponse<NotesResponse> {
        validateEntityType(entityType)
        val sort = if (paginationRequest.sortBy != null) {
            Sort.by(if (paginationRequest.sortDirection.name == "ASC") Sort.Direction.ASC else Sort.Direction.DESC, paginationRequest.sortBy)
        } else {
            Sort.by(Sort.Direction.DESC, "createdAt")
        }
        
        val pageable = PageRequest.of(paginationRequest.offset / paginationRequest.limit, paginationRequest.limit, sort)
        val notesPage = notesRepositoryWrapper.findAllByEntityTypeAndEntityId(entityType, entityId, pageable)
        
        return PaginatedResponse(
            content = notesPage.content.map { toNotesResponse(it) },
            pagination = PaginationInfo(
                offset = paginationRequest.offset,
                limit = paginationRequest.limit,
                totalElements = notesPage.totalElements,
                totalPages = notesPage.totalPages,
                currentPage = notesPage.number,
                hasNext = notesPage.hasNext(),
                hasPrevious = notesPage.hasPrevious()
            )
        )
    }

    private fun toNotesResponse(notes: Notes): NotesResponse {
        return NotesResponse(
            id = notes.id!!,
            title = notes.title,
            content = notes.content,
            entityType = notes.entityType,
            entityId = notes.entityId,
            createdAt = notes.createdAt!!,    
            updatedAt = notes.updatedAt!!,
            createdBy = notes.createdBy,
            updatedBy = notes.updatedBy
        )
    }
}