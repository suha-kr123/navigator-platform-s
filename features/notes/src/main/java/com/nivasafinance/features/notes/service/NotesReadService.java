package com.nivasafinance.features.notes.service;

import com.nivasafinance.features.notes.dto.NotesResponse;

import java.util.UUID;

public interface NotesReadService {
    NotesResponse getNoteById(Long notesId);
    NotesResponse getNoteByIdentifier(UUID notesId);
}

