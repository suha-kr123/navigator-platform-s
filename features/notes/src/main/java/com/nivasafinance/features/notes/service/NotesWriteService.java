package com.nivasafinance.features.notes.service;

import com.nivasafinance.features.notes.dto.NotesRequest;
import com.nivasafinance.features.notes.dto.NotesResponse;
import com.nivasafinance.features.notes.dto.NotesUpdateRequest;

import java.util.UUID;

public interface NotesWriteService {
    NotesResponse createNote(NotesRequest notesRequest);
    NotesResponse updateNote(UUID identifier, NotesUpdateRequest notesUpdateRequest);
    void deleteNote(UUID identifier);
}

