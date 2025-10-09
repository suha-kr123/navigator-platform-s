package com.nivasafinance.features.notes.service

import com.nivasafinance.common.base.model.PaginatedResponse
import com.nivasafinance.common.base.model.PaginationInfo
import com.nivasafinance.common.base.model.PaginationRequest
import com.nivasafinance.features.notes.dto.NotesRequest
import com.nivasafinance.features.notes.dto.NotesResponse
import com.nivasafinance.features.notes.dto.NotesUpdateRequest
import com.nivasafinance.features.notes.entity.Notes
import com.nivasafinance.features.notes.repository.NotesRepositoryWrapper
import org.springframework.context.MessageSource
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class NotesServiceImpl(
    private val notesRepositoryWrapper: NotesRepositoryWrapper,
    private val messageSource: MessageSource
) : NotesService {

    override fun createNotes(notesRequest: NotesRequest): NotesResponse {
        val notes = Notes(
            title = notesRequest.title,
            content = notesRequest.content
        )
        val savedNotes = notesRepositoryWrapper.saveWithException(notes)
        return toNotesResponse(savedNotes)
    }

    override fun updateNotes(notesId: UUID, notesUpdateRequest: NotesUpdateRequest): NotesResponse {
        val existingNotes = notesRepositoryWrapper.findByIdWithException(notesId)

        // Update the fields directly like in patchNotes
        if (notesUpdateRequest.title != null) {
            existingNotes.title = notesUpdateRequest.title
        }
        if (notesUpdateRequest.content != null) {
            existingNotes.content = notesUpdateRequest.content
        }

        val savedNotes = notesRepositoryWrapper.saveWithException(existingNotes)
        return toNotesResponse(savedNotes)
    }

    override fun patchNotes(notesId: UUID, notesUpdateRequest: NotesUpdateRequest): NotesResponse {
        val existingNotes = notesRepositoryWrapper.findByIdWithException(notesId)

        // Update only the fields that are provided in the request
        if (notesUpdateRequest.title != null) {
            existingNotes.title = notesUpdateRequest.title
        }
        if (notesUpdateRequest.content != null) {
            existingNotes.content = notesUpdateRequest.content
        }

        val savedNotes = notesRepositoryWrapper.saveWithException(existingNotes)
        return toNotesResponse(savedNotes)
    }

    override fun deleteNotes(notesId: UUID) {
        notesRepositoryWrapper.deleteByIdWithException(notesId)
    }

    override fun getNotesById(notesId: UUID): NotesResponse {
        val notes = notesRepositoryWrapper.findByIdWithException(notesId)
        return toNotesResponse(notes)
    }

    override fun getAllNotes(paginationRequest: PaginationRequest): PaginatedResponse<NotesResponse> {
        val sort = if (paginationRequest.sortBy != null) {
            Sort.by(
                if (paginationRequest.sortDirection == "ASC") Sort.Direction.ASC else Sort.Direction.DESC,
                paginationRequest.sortBy
            )
        } else {
            Sort.by(Sort.Direction.DESC, "createdAt")
        }

        val pageable = PageRequest.of(paginationRequest.offset / paginationRequest.limit, paginationRequest.limit, sort)
        val notesPage = notesRepositoryWrapper.findAll(pageable)

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
            createdAt = notes.createdAt!!,
            updatedAt = notes.updatedAt!!,
            createdBy = notes.createdBy,
            updatedBy = notes.updatedBy
        )
    }
}
