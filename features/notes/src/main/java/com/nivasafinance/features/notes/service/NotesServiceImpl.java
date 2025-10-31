package com.nivasafinance.features.notes.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.notes.dto.NotesRequest;
import com.nivasafinance.features.notes.dto.NotesResponse;
import com.nivasafinance.features.notes.dto.NotesUpdateRequest;
import com.nivasafinance.features.notes.entity.Notes;
import com.nivasafinance.features.notes.repository.NotesRepositoryWrapper;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotesServiceImpl implements NotesService {

    private final NotesRepositoryWrapper notesRepositoryWrapper;
    private final MessageSource messageSource;

    public NotesServiceImpl(NotesRepositoryWrapper notesRepositoryWrapper, MessageSource messageSource) {
        this.notesRepositoryWrapper = notesRepositoryWrapper;
        this.messageSource = messageSource;
    }

    @Override
    public NotesResponse createNotes(NotesRequest notesRequest) {
        Notes notes = new Notes(notesRequest.getTitle(), notesRequest.getContent());
        Notes savedNotes = notesRepositoryWrapper.saveWithException(notes);
        return toNotesResponse(savedNotes);
    }

    @Override
    public NotesResponse updateNotes(UUID notesId, NotesUpdateRequest notesUpdateRequest) {
        Notes existingNotes = notesRepositoryWrapper.findByIdWithException(notesId);

        // Update the fields directly like in patchNotes
        if (notesUpdateRequest.getTitle() != null) {
            existingNotes.setTitle(notesUpdateRequest.getTitle());
        }
        if (notesUpdateRequest.getContent() != null) {
            existingNotes.setContent(notesUpdateRequest.getContent());
        }

        Notes savedNotes = notesRepositoryWrapper.saveWithException(existingNotes);
        return toNotesResponse(savedNotes);
    }

    @Override
    public NotesResponse patchNotes(UUID notesId, NotesUpdateRequest notesUpdateRequest) {
        Notes existingNotes = notesRepositoryWrapper.findByIdWithException(notesId);

        // Update only the fields that are provided in the request
        if (notesUpdateRequest.getTitle() != null) {
            existingNotes.setTitle(notesUpdateRequest.getTitle());
        }
        if (notesUpdateRequest.getContent() != null) {
            existingNotes.setContent(notesUpdateRequest.getContent());
        }

        Notes savedNotes = notesRepositoryWrapper.saveWithException(existingNotes);
        return toNotesResponse(savedNotes);
    }

    @Override
    public void deleteNotes(UUID notesId) {
        notesRepositoryWrapper.deleteByIdWithException(notesId);
    }

    @Override
    public NotesResponse getNotesById(UUID notesId) {
        Notes notes = notesRepositoryWrapper.findByIdWithException(notesId);
        return toNotesResponse(notes);
    }

    @Override
    public PaginatedResponse<NotesResponse> getAllNotes(PaginationRequest paginationRequest) {
        Sort sort;
        if (paginationRequest.getSortBy() != null) {
            Sort.Direction direction = "ASC".equals(paginationRequest.getSortDirection())
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;
            sort = Sort.by(direction, paginationRequest.getSortBy());
        } else {
            sort = Sort.by(Sort.Direction.DESC, "createdAt");
        }

        Pageable pageable = PageRequest.of(
                paginationRequest.getOffset() / paginationRequest.getLimit(),
                paginationRequest.getLimit(),
                sort
        );
        Page<Notes> notesPage = notesRepositoryWrapper.findAll(pageable);

        List<NotesResponse> responseList = notesPage.getContent().stream()
                .map(this::toNotesResponse)
                .collect(Collectors.toList());

        PaginationInfo paginationInfo = new PaginationInfo(
                paginationRequest.getOffset(),
                paginationRequest.getLimit(),
                notesPage.getTotalElements(),
                notesPage.getTotalPages(),
                notesPage.getNumber(),
                notesPage.hasNext(),
                notesPage.hasPrevious()
        );

        return new PaginatedResponse<>(responseList, paginationInfo);
    }

    private NotesResponse toNotesResponse(Notes notes) {
        return new NotesResponse(
                notes.getId(),
                notes.getTitle(),
                notes.getContent(),
                notes.getCreatedAt(),
                notes.getUpdatedAt(),
                notes.getCreatedBy(),
                notes.getUpdatedBy()
        );
    }
}

