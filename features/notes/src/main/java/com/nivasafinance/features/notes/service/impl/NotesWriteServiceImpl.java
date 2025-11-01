package com.nivasafinance.features.notes.service.impl;

import com.nivasafinance.features.notes.dto.NotesRequest;
import com.nivasafinance.features.notes.dto.NotesResponse;
import com.nivasafinance.features.notes.dto.NotesUpdateRequest;
import com.nivasafinance.features.notes.entity.Notes;
import com.nivasafinance.features.notes.repository.NotesRepositoryWrapper;
import com.nivasafinance.features.notes.service.NotesWriteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class NotesWriteServiceImpl implements NotesWriteService {

    private final NotesRepositoryWrapper notesRepositoryWrapper;

    public NotesWriteServiceImpl(NotesRepositoryWrapper notesRepositoryWrapper) {
        this.notesRepositoryWrapper = notesRepositoryWrapper;
    }

    @Override
    public NotesResponse createNote(NotesRequest notesRequest) {
        Notes notes = new Notes();
        notes.setIdentifier(UUID.randomUUID());
        notes.setTitle(notesRequest.getTitle());
        notes.setContent(notesRequest.getContent());

        Notes savedNotes = notesRepositoryWrapper.saveWithException(notes);
        return NotesResponse.toNotesResponse(savedNotes);
    }

    @Override
    public NotesResponse updateNote(UUID identifier, NotesUpdateRequest notesUpdateRequest) {
        Notes existingNotes = notesRepositoryWrapper.findByIdentifierWithException(identifier);

        existingNotes.setTitle(notesUpdateRequest.getTitle());
        existingNotes.setContent(notesUpdateRequest.getContent());

        Notes updatedNotes = notesRepositoryWrapper.saveWithException(existingNotes);
        return NotesResponse.toNotesResponse(updatedNotes);
    }

    @Override
    public void deleteNote(UUID identifier) {
        Notes notes = notesRepositoryWrapper.findByIdentifierWithException(identifier);
        notesRepositoryWrapper.deleteByIdWithException(notes.getId());
    }
}

