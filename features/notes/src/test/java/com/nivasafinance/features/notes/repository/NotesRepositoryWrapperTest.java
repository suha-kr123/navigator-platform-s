package com.nivasafinance.features.notes.repository;

import com.nivasafinance.features.notes.entity.Notes;
import com.nivasafinance.features.notes.exception.NotesNotFoundException;
import com.nivasafinance.features.notes.exception.NotesOperationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotesRepositoryWrapperTest {

    @Mock
    private NotesRepository notesRepository;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private NotesRepositoryWrapper wrapper;

    // ── saveWithException ──

    @Test
    void saveWithException_whenSuccess_returnsSavedNotes() {
        Notes notes = buildNotes(1L, UUID.randomUUID(), "Title", "Content");
        when(notesRepository.save(notes)).thenReturn(notes);

        Notes result = wrapper.saveWithException(notes);

        assertEquals(notes.getId(), result.getId(),
                "Saved note ID should match the input note ID");
        verify(notesRepository).save(notes);
    }

    @Test
    void saveWithException_whenRuntimeException_throwsNotesOperationException() {
        Notes notes = new Notes();
        when(notesRepository.save(notes)).thenThrow(new RuntimeException("DB error"));

        assertThrows(NotesOperationException.class,
                () -> wrapper.saveWithException(notes),
                "Repository save failure should throw NotesOperationException");
    }

    // ── deleteByIdWithException ──

    @Test
    void deleteByIdWithException_whenSuccess_completes() {
        doNothing().when(notesRepository).deleteById(1L);

        assertDoesNotThrow(() -> wrapper.deleteByIdWithException(1L),
                "Successful delete should not throw any exception");
        verify(notesRepository).deleteById(1L);
    }

    @Test
    void deleteByIdWithException_whenRuntimeException_throwsNotesOperationException() {
        doThrow(new RuntimeException("DB error")).when(notesRepository).deleteById(1L);

        assertThrows(NotesOperationException.class,
                () -> wrapper.deleteByIdWithException(1L),
                "Repository delete failure should throw NotesOperationException");
    }

    // ── findByIdWithException ──

    @Test
    void findByIdWithException_whenFound_returnsNotes() {
        Notes notes = buildNotes(1L, UUID.randomUUID(), "Title", "Content");
        when(notesRepository.findById(1L)).thenReturn(Optional.of(notes));

        Notes result = wrapper.findByIdWithException(1L);

        assertEquals(1L, result.getId(),
                "Returned note should have the requested ID");
    }

    @Test
    void findByIdWithException_whenNotFound_throwsNotesNotFoundException() {
        when(notesRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotesNotFoundException.class,
                () -> wrapper.findByIdWithException(99L),
                "Missing note by ID should throw NotesNotFoundException");
    }

    // ── findByIdentifierWithException ──

    @Test
    void findByIdentifierWithException_whenFound_returnsNotes() {
        UUID identifier = UUID.randomUUID();
        Notes notes = buildNotes(1L, identifier, "Title", "Content");
        when(notesRepository.findByIdentifier(identifier)).thenReturn(Optional.of(notes));

        Notes result = wrapper.findByIdentifierWithException(identifier);

        assertEquals(identifier, result.getIdentifier(),
                "Returned note should have the requested identifier");
    }

    @Test
    void findByIdentifierWithException_whenNotFound_throwsNotesNotFoundException() {
        UUID identifier = UUID.randomUUID();
        when(notesRepository.findByIdentifier(identifier)).thenReturn(Optional.empty());

        assertThrows(NotesNotFoundException.class,
                () -> wrapper.findByIdentifierWithException(identifier),
                "Missing note by identifier should throw NotesNotFoundException");
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
