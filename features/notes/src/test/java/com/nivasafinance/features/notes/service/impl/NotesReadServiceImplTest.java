package com.nivasafinance.features.notes.service.impl;

import com.nivasafinance.features.notes.dto.NotesResponse;
import com.nivasafinance.features.notes.entity.Notes;
import com.nivasafinance.features.notes.exception.NotesNotFoundException;
import com.nivasafinance.features.notes.repository.NotesRepositoryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotesReadServiceImplTest {

    @Mock
    private NotesRepositoryWrapper notesRepositoryWrapper;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private NotesReadServiceImpl service;

    // ── getNoteById ──

    @Test
    void getNoteById_whenNoteExists_returnsNotesResponse() {
        Notes notes = buildNotes(1L, UUID.randomUUID(), "Title", "Content");
        when(notesRepositoryWrapper.findByIdWithException(1L)).thenReturn(notes);

        NotesResponse result = service.getNoteById(1L);

        assertEquals(notes.getId(), result.getId(),
                "Response ID should match the entity ID");
        assertEquals(notes.getTitle(), result.getTitle(),
                "Response title should match the entity title");
        assertEquals(notes.getContent(), result.getContent(),
                "Response content should match the entity content");
        verify(notesRepositoryWrapper).findByIdWithException(1L);
    }

    @Test
    void getNoteById_whenNotFound_propagatesException() {
        when(messageSource.getMessage(anyString(), any(), any(java.util.Locale.class)))
                .thenReturn("Note not found");
        NotesNotFoundException exception = new NotesNotFoundException(99L, messageSource);
        when(notesRepositoryWrapper.findByIdWithException(99L))
                .thenThrow(exception);

        assertThrows(NotesNotFoundException.class,
                () -> service.getNoteById(99L),
                "Should propagate NotesNotFoundException from repository wrapper");
    }

    // ── getNoteByIdentifier ──

    @Test
    void getNoteByIdentifier_whenNoteExists_returnsNotesResponse() {
        UUID identifier = UUID.randomUUID();
        Notes notes = buildNotes(1L, identifier, "Title", "Content");
        when(notesRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(notes);

        NotesResponse result = service.getNoteByIdentifier(identifier);

        assertEquals(identifier, result.getIdentifier(),
                "Response identifier should match the requested identifier");
        assertEquals(notes.getTitle(), result.getTitle(),
                "Response title should match the entity title");
        verify(notesRepositoryWrapper).findByIdentifierWithException(identifier);
    }

    @Test
    void getNoteByIdentifier_whenNotFound_propagatesException() {
        UUID identifier = UUID.randomUUID();
        when(messageSource.getMessage(anyString(), any(), any(java.util.Locale.class)))
                .thenReturn("Note not found");
        NotesNotFoundException exception = new NotesNotFoundException(identifier, messageSource);
        when(notesRepositoryWrapper.findByIdentifierWithException(identifier))
                .thenThrow(exception);

        assertThrows(NotesNotFoundException.class,
                () -> service.getNoteByIdentifier(identifier),
                "Should propagate NotesNotFoundException from repository wrapper");
    }

    // ── helper ──

    private Notes buildNotes(Long id, UUID identifier, String title, String content) {
        Notes notes = new Notes();
        notes.setId(id);
        notes.setIdentifier(identifier);
        notes.setTitle(title);
        notes.setContent(content);
        return notes;
    }
}
