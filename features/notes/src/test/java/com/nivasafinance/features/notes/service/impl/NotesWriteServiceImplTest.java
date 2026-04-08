package com.nivasafinance.features.notes.service.impl;

import com.nivasafinance.features.notes.dto.NotesRequest;
import com.nivasafinance.features.notes.dto.NotesResponse;
import com.nivasafinance.features.notes.dto.NotesUpdateRequest;
import com.nivasafinance.features.notes.entity.Notes;
import com.nivasafinance.features.notes.exception.NotesNotFoundException;
import com.nivasafinance.features.notes.repository.NotesRepositoryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotesWriteServiceImplTest {

    @Mock
    private NotesRepositoryWrapper notesRepositoryWrapper;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private NotesWriteServiceImpl service;

    // ── createNote ──

    @Test
    void createNote_whenValid_returnsNotesResponse() {
        NotesRequest request = new NotesRequest("Title", "Content");
        Notes savedNotes = buildNotes(1L, UUID.randomUUID(), "Title", "Content");
        when(notesRepositoryWrapper.saveWithException(any(Notes.class))).thenReturn(savedNotes);

        NotesResponse result = service.createNote(request);

        assertEquals(savedNotes.getId(), result.getId(),
                "Response ID should match the persisted note ID");
        assertEquals("Title", result.getTitle(),
                "Response title should match the request title");
        assertEquals("Content", result.getContent(),
                "Response content should match the request content");
    }

    @Test
    void createNote_setsIdentifierBeforeSave() {
        NotesRequest request = new NotesRequest("Title", "Content");
        Notes savedNotes = buildNotes(1L, UUID.randomUUID(), "Title", "Content");
        when(notesRepositoryWrapper.saveWithException(any(Notes.class))).thenReturn(savedNotes);

        service.createNote(request);

        ArgumentCaptor<Notes> captor = ArgumentCaptor.forClass(Notes.class);
        verify(notesRepositoryWrapper).saveWithException(captor.capture());
        assertNotNull(captor.getValue().getIdentifier(),
                "Identifier must be set before saving the note");
    }

    // ── updateNote ──

    @Test
    void updateNote_whenNoteExists_returnsUpdatedResponse() {
        UUID identifier = UUID.randomUUID();
        Notes existingNotes = buildNotes(1L, identifier, "Old Title", "Old Content");
        Notes updatedNotes = buildNotes(1L, identifier, "New Title", "New Content");

        when(notesRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(existingNotes);
        when(notesRepositoryWrapper.saveWithException(existingNotes)).thenReturn(updatedNotes);

        NotesUpdateRequest updateRequest = new NotesUpdateRequest("New Title", "New Content");
        NotesResponse result = service.updateNote(identifier, updateRequest);

        assertEquals("New Title", result.getTitle(),
                "Response should reflect the updated title");
        assertEquals("New Content", result.getContent(),
                "Response should reflect the updated content");
    }

    @Test
    void updateNote_updatesFieldsFromRequest() {
        UUID identifier = UUID.randomUUID();
        Notes existingNotes = buildNotes(1L, identifier, "Old Title", "Old Content");

        when(notesRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(existingNotes);
        when(notesRepositoryWrapper.saveWithException(existingNotes)).thenReturn(existingNotes);

        NotesUpdateRequest updateRequest = new NotesUpdateRequest("New Title", "New Content");
        service.updateNote(identifier, updateRequest);

        assertEquals("New Title", existingNotes.getTitle(),
                "Entity title should be updated from the request before save");
        assertEquals("New Content", existingNotes.getContent(),
                "Entity content should be updated from the request before save");
        verify(notesRepositoryWrapper).saveWithException(existingNotes);
    }

    @Test
    void updateNote_whenNotFound_propagatesException() {
        UUID identifier = UUID.randomUUID();
        when(messageSource.getMessage(anyString(), any(), any(java.util.Locale.class)))
                .thenReturn("Note not found");
        NotesNotFoundException exception = new NotesNotFoundException(identifier, messageSource);
        when(notesRepositoryWrapper.findByIdentifierWithException(identifier))
                .thenThrow(exception);

        NotesUpdateRequest updateRequest = new NotesUpdateRequest("Title", "Content");

        assertThrows(NotesNotFoundException.class,
                () -> service.updateNote(identifier, updateRequest),
                "Should propagate NotesNotFoundException when note does not exist");
        verifyNoMoreInteractions(notesRepositoryWrapper);
    }

    // ── deleteNote ──

    @Test
    void deleteNote_whenNoteExists_deletesSuccessfully() {
        UUID identifier = UUID.randomUUID();
        Notes notes = buildNotes(10L, identifier, "Title", "Content");
        when(notesRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(notes);
        doNothing().when(notesRepositoryWrapper).deleteByIdWithException(10L);

        assertDoesNotThrow(() -> service.deleteNote(identifier),
                "Delete should complete without exception when note exists");
    }

    @Test
    void deleteNote_verifiesDeleteCalledWithCorrectId() {
        UUID identifier = UUID.randomUUID();
        Notes notes = buildNotes(10L, identifier, "Title", "Content");
        when(notesRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(notes);
        doNothing().when(notesRepositoryWrapper).deleteByIdWithException(10L);

        service.deleteNote(identifier);

        verify(notesRepositoryWrapper).findByIdentifierWithException(identifier);
        verify(notesRepositoryWrapper).deleteByIdWithException(10L);
    }

    @Test
    void deleteNote_whenNotFound_propagatesException() {
        UUID identifier = UUID.randomUUID();
        when(messageSource.getMessage(anyString(), any(), any(java.util.Locale.class)))
                .thenReturn("Note not found");
        NotesNotFoundException exception = new NotesNotFoundException(identifier, messageSource);
        when(notesRepositoryWrapper.findByIdentifierWithException(identifier))
                .thenThrow(exception);

        assertThrows(NotesNotFoundException.class,
                () -> service.deleteNote(identifier),
                "Should propagate NotesNotFoundException when note does not exist");
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
