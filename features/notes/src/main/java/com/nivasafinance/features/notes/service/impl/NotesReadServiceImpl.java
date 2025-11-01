package com.nivasafinance.features.notes.service.impl;

import com.nivasafinance.features.notes.dto.NotesResponse;
import com.nivasafinance.features.notes.entity.Notes;
import com.nivasafinance.features.notes.repository.NotesRepositoryWrapper;
import com.nivasafinance.features.notes.service.NotesReadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class NotesReadServiceImpl implements NotesReadService {

    private final NotesRepositoryWrapper notesRepositoryWrapper;

    public NotesReadServiceImpl(NotesRepositoryWrapper notesRepositoryWrapper) {
        this.notesRepositoryWrapper = notesRepositoryWrapper;
    }

    @Override
    public NotesResponse getNoteById(Long notesId) {
        Notes notes = notesRepositoryWrapper.findByIdWithException(notesId);
        return NotesResponse.toNotesResponse(notes);
    }

    @Override
    public NotesResponse getNoteByIdentifier(UUID notesId) {
        Notes notes = notesRepositoryWrapper.findByIdentifierWithException(notesId);
        return NotesResponse.toNotesResponse(notes);
    }
}

