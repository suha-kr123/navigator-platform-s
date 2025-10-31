package com.nivasafinance.features.notes.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.notes.dto.NotesRequest;
import com.nivasafinance.features.notes.dto.NotesResponse;
import com.nivasafinance.features.notes.dto.NotesUpdateRequest;
import java.util.UUID;

public interface NotesService {
    NotesResponse createNotes(NotesRequest notesRequest);
    NotesResponse updateNotes(UUID notesId, NotesUpdateRequest notesUpdateRequest);
    NotesResponse patchNotes(UUID notesId, NotesUpdateRequest notesUpdateRequest);
    void deleteNotes(UUID notesId);
    NotesResponse getNotesById(UUID notesId);
    PaginatedResponse<NotesResponse> getAllNotes(PaginationRequest paginationRequest);
}

