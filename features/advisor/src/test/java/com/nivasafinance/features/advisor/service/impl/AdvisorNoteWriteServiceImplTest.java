package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.features.advisor.dto.AdvisorNoteCreateRequest;
import com.nivasafinance.features.advisor.dto.AdvisorNoteCreateResponse;
import com.nivasafinance.features.advisor.dto.AdvisorNoteUpdateRequest;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.notes.dto.NotesRequest;
import com.nivasafinance.features.notes.dto.NotesResponse;
import com.nivasafinance.features.notes.dto.NotesUpdateRequest;
import com.nivasafinance.features.notes.service.NotesReadService;
import com.nivasafinance.features.notes.service.NotesWriteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvisorNoteWriteServiceImplTest {

    @Mock
    private NotesWriteService notesWriteService;

    @Mock
    private NotesReadService notesReadService;

    @Mock
    private AdvisorRepositoryWrapper advisorRepositoryWrapper;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private AdvisorNoteWriteServiceImpl advisorNoteWriteService;

    private UUID advisorIdentifier;
    private UUID noteIdentifier;
    private Advisor advisor;
    private AdvisorNoteCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        advisorIdentifier = UUID.randomUUID();
        noteIdentifier = UUID.randomUUID();
        advisor = new Advisor();
        advisor.setId(1L);
        advisor.setIdentifier(advisorIdentifier);
        advisor.setNotes(new ArrayList<>());

        createRequest = new AdvisorNoteCreateRequest();
        createRequest.setTitle("Title");
        createRequest.setContent("Content");
    }

    @Test
    void createAdvisorNote_success_returnsNoteIdentifier() {
        NotesResponse notesResponse = new NotesResponse();
        notesResponse.setId(10L);
        notesResponse.setIdentifier(noteIdentifier);
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(notesWriteService.createNote(any(NotesRequest.class))).thenReturn(notesResponse);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");

            AdvisorNoteCreateResponse result = advisorNoteWriteService.createAdvisorNote(advisorIdentifier, createRequest);

            assertNotNull(result);
            assertEquals(noteIdentifier, result.getNoteIdentifier());
            verify(notesWriteService).createNote(any(NotesRequest.class));
            verify(advisorRepositoryWrapper).saveWithException(advisor);
            assertTrue(advisor.getNotes().contains(10L));
        }
    }

    @Test
    void createAdvisorNote_advisorWithNullNotes_initializesList() {
        advisor.setNotes(null);
        NotesResponse notesResponse = new NotesResponse();
        notesResponse.setId(10L);
        notesResponse.setIdentifier(noteIdentifier);
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(notesWriteService.createNote(any(NotesRequest.class))).thenReturn(notesResponse);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");

            advisorNoteWriteService.createAdvisorNote(advisorIdentifier, createRequest);

            assertNotNull(advisor.getNotes());
            assertTrue(advisor.getNotes().contains(10L));
        }
    }

    @Test
    void updateAdvisorNote_success() {
        AdvisorNoteUpdateRequest updateRequest = new AdvisorNoteUpdateRequest();
        updateRequest.setTitle("New Title");
        updateRequest.setContent("New Content");
        NotesResponse note = new NotesResponse();
        note.setId(10L);
        note.setIdentifier(noteIdentifier);
        advisor.setNotes(List.of(10L));

        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(notesReadService.getNoteByIdentifier(noteIdentifier)).thenReturn(note);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");

            advisorNoteWriteService.updateAdvisorNote(advisorIdentifier, noteIdentifier, updateRequest);

            verify(notesWriteService).updateNote(eq(noteIdentifier), any(NotesUpdateRequest.class));
            verify(applicationEventPublisher, atLeast(1)).publishEvent(any(Object.class));
        }
    }

    @Test
    void updateAdvisorNote_noteNotBelongToAdvisor_throwsException() {
        AdvisorNoteUpdateRequest updateRequest = new AdvisorNoteUpdateRequest();
        NotesResponse note = new NotesResponse();
        note.setId(10L);
        note.setIdentifier(noteIdentifier);
        advisor.setNotes(List.of(99L));

        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(notesReadService.getNoteByIdentifier(noteIdentifier)).thenReturn(note);

        assertThrows(RuntimeException.class,
                () -> advisorNoteWriteService.updateAdvisorNote(advisorIdentifier, noteIdentifier, updateRequest));
        verify(notesWriteService, never()).updateNote(any(), any());
    }

    @Test
    void updateAdvisorNote_nullNotesList_throwsException() {
        AdvisorNoteUpdateRequest updateRequest = new AdvisorNoteUpdateRequest();
        NotesResponse note = new NotesResponse();
        note.setId(10L);
        note.setIdentifier(noteIdentifier);
        advisor.setNotes(null);

        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(notesReadService.getNoteByIdentifier(noteIdentifier)).thenReturn(note);

        assertThrows(RuntimeException.class,
                () -> advisorNoteWriteService.updateAdvisorNote(advisorIdentifier, noteIdentifier, updateRequest));
        verify(notesWriteService, never()).updateNote(any(), any());
    }

    @Test
    void deleteAdvisorNote_success() {
        NotesResponse note = new NotesResponse();
        note.setId(10L);
        note.setIdentifier(noteIdentifier);
        advisor.setNotes(new ArrayList<>(List.of(10L)));

        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(notesReadService.getNoteByIdentifier(noteIdentifier)).thenReturn(note);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");

            advisorNoteWriteService.deleteAdvisorNote(advisorIdentifier, noteIdentifier);

            verify(notesWriteService).deleteNote(noteIdentifier);
            assertFalse(advisor.getNotes().contains(10L));
            verify(applicationEventPublisher, atLeast(1)).publishEvent(any(Object.class));
        }
    }

    @Test
    void deleteAdvisorNote_noteNotBelongToAdvisor_throwsException() {
        NotesResponse note = new NotesResponse();
        note.setId(10L);
        note.setIdentifier(noteIdentifier);
        advisor.setNotes(List.of(99L));

        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(notesReadService.getNoteByIdentifier(noteIdentifier)).thenReturn(note);

        assertThrows(RuntimeException.class,
                () -> advisorNoteWriteService.deleteAdvisorNote(advisorIdentifier, noteIdentifier));
        verify(notesWriteService, never()).deleteNote(any());
    }

    @Test
    void deleteAdvisorNote_nullNotesList_stillDeletesNoteAndDoesNotSaveAdvisor() {
        NotesResponse note = new NotesResponse();
        note.setId(10L);
        note.setIdentifier(noteIdentifier);
        advisor.setNotes(null);

        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(notesReadService.getNoteByIdentifier(noteIdentifier)).thenReturn(note);

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");

            advisorNoteWriteService.deleteAdvisorNote(advisorIdentifier, noteIdentifier);

            verify(advisorRepositoryWrapper, never()).saveWithException(any());
            verify(notesWriteService).deleteNote(noteIdentifier);
            verify(applicationEventPublisher, atLeast(1)).publishEvent(any(Object.class));
        }
    }
}
