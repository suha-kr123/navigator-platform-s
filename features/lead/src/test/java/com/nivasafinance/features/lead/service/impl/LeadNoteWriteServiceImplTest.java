package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.features.lead.dto.LeadNoteCreateRequest;
import com.nivasafinance.features.lead.dto.LeadNoteCreateResponse;
import com.nivasafinance.features.lead.dto.LeadNoteUpdateRequest;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.notes.dto.NotesRequest;
import com.nivasafinance.features.notes.dto.NotesResponse;
import com.nivasafinance.features.notes.dto.NotesUpdateRequest;
import com.nivasafinance.features.notes.service.NotesReadService;
import com.nivasafinance.features.notes.service.NotesWriteService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadNoteWriteServiceImplTest {

    @Mock
    private NotesWriteService notesWriteService;

    @Mock
    private NotesReadService notesReadService;

    @Mock
    private LeadRepositoryWrapper leadRepositoryWrapper;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private LeadNoteWriteServiceImpl leadNoteWriteService;

    private UUID leadIdentifier;
    private UUID noteIdentifier;
    private Long leadId;
    private Long noteId;
    private Lead lead;

    @BeforeEach
    void setUp() {
        UserContext.setUsername("test-user");

        leadIdentifier = UUID.randomUUID();
        noteIdentifier = UUID.randomUUID();
        leadId = 1L;
        noteId = 100L;

        lead = new Lead();
        lead.setId(leadId);
        lead.setLeadIdentifier(leadIdentifier);
        lead.setNotes(new ArrayList<>());
    }

    @AfterEach
    void tearDown() {
        UserContext.setUsername(null);
    }

    // ==================== createLeadNote() Tests ====================

    @Test
    void createLeadNote_withValidRequest_createsNoteAndAddsToLead() {
        // Arrange
        LeadNoteCreateRequest request = LeadNoteCreateRequest.builder()
                .title("Follow-up").content("Call scheduled for Monday").build();

        NotesResponse notesResponse = new NotesResponse();
        notesResponse.setId(noteId);
        notesResponse.setIdentifier(noteIdentifier);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(notesWriteService.createNote(any(NotesRequest.class))).thenReturn(notesResponse);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        LeadNoteCreateResponse result = leadNoteWriteService.createLeadNote(leadIdentifier, request);

        // Assert
        assertNotNull(result, "Response should not be null");
        assertEquals(noteIdentifier, result.getNoteIdentifier(), "Note identifier should match the created note");
        assertTrue(lead.getNotes().contains(noteId), "Lead's notes list should contain the new note ID");
        verify(leadRepositoryWrapper).saveWithException(lead);
        verify(applicationEventPublisher).publishEvent(any(SystemEvent.class));
    }

    @Test
    void createLeadNote_withNullNotesList_initializesListAndAddsNote() {
        // Arrange
        lead.setNotes(null);
        LeadNoteCreateRequest request = LeadNoteCreateRequest.builder()
                .title("Initial note").content("First note on this lead").build();

        NotesResponse notesResponse = new NotesResponse();
        notesResponse.setId(noteId);
        notesResponse.setIdentifier(noteIdentifier);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(notesWriteService.createNote(any(NotesRequest.class))).thenReturn(notesResponse);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        LeadNoteCreateResponse result = leadNoteWriteService.createLeadNote(leadIdentifier, request);

        // Assert
        assertNotNull(result, "Response should not be null");
        assertNotNull(lead.getNotes(), "Notes list should be initialized");
        assertTrue(lead.getNotes().contains(noteId), "Initialized notes list should contain the new note ID");
    }

    @Test
    void createLeadNote_publishesCreationEvent() {
        // Arrange
        LeadNoteCreateRequest request = LeadNoteCreateRequest.builder()
                .title("Event test").content("Verify event").build();

        NotesResponse notesResponse = new NotesResponse();
        notesResponse.setId(noteId);
        notesResponse.setIdentifier(noteIdentifier);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(notesWriteService.createNote(any(NotesRequest.class))).thenReturn(notesResponse);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadNoteWriteService.createLeadNote(leadIdentifier, request);

        // Assert
        ArgumentCaptor<SystemEvent<?>> eventCaptor = ArgumentCaptor.forClass(SystemEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        SystemEvent<?> event = eventCaptor.getValue();
        assertEquals("LEAD_NOTE_CREATED", event.getEventType(), "Event type should be LEAD_NOTE_CREATED");
        assertEquals("test-user", event.getUsername(), "Event username should match UserContext");
    }

    // ==================== updateLeadNote() Tests ====================

    @Test
    void updateLeadNote_withValidRequest_updatesNote() {
        // Arrange
        lead.setNotes(new ArrayList<>(List.of(noteId)));
        LeadNoteUpdateRequest request = LeadNoteUpdateRequest.builder()
                .title("Updated title").content("Updated content").build();

        NotesResponse notesResponse = new NotesResponse();
        notesResponse.setId(noteId);
        notesResponse.setIdentifier(noteIdentifier);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(notesReadService.getNoteByIdentifier(noteIdentifier)).thenReturn(notesResponse);

        // Act
        leadNoteWriteService.updateLeadNote(leadIdentifier, noteIdentifier, request);

        // Assert
        verify(notesWriteService).updateNote(eq(noteIdentifier), any(NotesUpdateRequest.class));
        verify(applicationEventPublisher).publishEvent(any(SystemEvent.class));
    }

    @Test
    void updateLeadNote_whenNoteNotBelongingToLead_throwsRuntimeException() {
        // Arrange
        lead.setNotes(new ArrayList<>(List.of(999L)));
        LeadNoteUpdateRequest request = LeadNoteUpdateRequest.builder()
                .title("Update").content("Content").build();

        NotesResponse notesResponse = new NotesResponse();
        notesResponse.setId(noteId);
        notesResponse.setIdentifier(noteIdentifier);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(notesReadService.getNoteByIdentifier(noteIdentifier)).thenReturn(notesResponse);

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> leadNoteWriteService.updateLeadNote(leadIdentifier, noteIdentifier, request),
                "Should throw when note does not belong to lead");
        verifyNoInteractions(notesWriteService);
    }

    @Test
    void updateLeadNote_whenNotesListIsNull_throwsRuntimeException() {
        // Arrange
        lead.setNotes(null);
        LeadNoteUpdateRequest request = LeadNoteUpdateRequest.builder()
                .title("Update").content("Content").build();

        NotesResponse notesResponse = new NotesResponse();
        notesResponse.setId(noteId);
        notesResponse.setIdentifier(noteIdentifier);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(notesReadService.getNoteByIdentifier(noteIdentifier)).thenReturn(notesResponse);

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> leadNoteWriteService.updateLeadNote(leadIdentifier, noteIdentifier, request),
                "Should throw when lead has null notes list");
    }

    @Test
    void updateLeadNote_publishesUpdateEvent() {
        // Arrange
        lead.setNotes(new ArrayList<>(List.of(noteId)));
        LeadNoteUpdateRequest request = LeadNoteUpdateRequest.builder()
                .title("Updated").content("Updated content").build();

        NotesResponse notesResponse = new NotesResponse();
        notesResponse.setId(noteId);
        notesResponse.setIdentifier(noteIdentifier);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(notesReadService.getNoteByIdentifier(noteIdentifier)).thenReturn(notesResponse);

        // Act
        leadNoteWriteService.updateLeadNote(leadIdentifier, noteIdentifier, request);

        // Assert
        ArgumentCaptor<SystemEvent<?>> eventCaptor = ArgumentCaptor.forClass(SystemEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        assertEquals("LEAD_NOTE_UPDATED", eventCaptor.getValue().getEventType(),
                "Event type should be LEAD_NOTE_UPDATED");
    }

    // ==================== deleteLeadNote() Tests ====================

    @Test
    void deleteLeadNote_withValidRequest_removesNoteFromLeadAndDeletes() {
        // Arrange
        lead.setNotes(new ArrayList<>(List.of(noteId)));

        NotesResponse notesResponse = new NotesResponse();
        notesResponse.setId(noteId);
        notesResponse.setIdentifier(noteIdentifier);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(notesReadService.getNoteByIdentifier(noteIdentifier)).thenReturn(notesResponse);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadNoteWriteService.deleteLeadNote(leadIdentifier, noteIdentifier);

        // Assert
        assertFalse(lead.getNotes().contains(noteId), "Note ID should be removed from lead's notes list");
        verify(leadRepositoryWrapper).saveWithException(lead);
        verify(notesWriteService).deleteNote(noteIdentifier);
        verify(applicationEventPublisher).publishEvent(any(SystemEvent.class));
    }

    @Test
    void deleteLeadNote_whenNoteNotBelongingToLead_throwsRuntimeException() {
        // Arrange
        lead.setNotes(new ArrayList<>(List.of(999L)));

        NotesResponse notesResponse = new NotesResponse();
        notesResponse.setId(noteId);
        notesResponse.setIdentifier(noteIdentifier);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(notesReadService.getNoteByIdentifier(noteIdentifier)).thenReturn(notesResponse);

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> leadNoteWriteService.deleteLeadNote(leadIdentifier, noteIdentifier),
                "Should throw when note does not belong to lead");
        verify(notesWriteService, never()).deleteNote(any());
    }

    @Test
    void deleteLeadNote_whenNotesListIsNull_skipsRemovalButStillDeletes() {
        // Arrange
        lead.setNotes(null);

        NotesResponse notesResponse = new NotesResponse();
        notesResponse.setId(noteId);
        notesResponse.setIdentifier(noteIdentifier);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(notesReadService.getNoteByIdentifier(noteIdentifier)).thenReturn(notesResponse);

        // Act
        leadNoteWriteService.deleteLeadNote(leadIdentifier, noteIdentifier);

        // Assert
        verify(leadRepositoryWrapper, never()).saveWithException(any());
        verify(notesWriteService).deleteNote(noteIdentifier);
    }

    @Test
    void deleteLeadNote_publishesDeletionEvent() {
        // Arrange
        lead.setNotes(new ArrayList<>(List.of(noteId)));

        NotesResponse notesResponse = new NotesResponse();
        notesResponse.setId(noteId);
        notesResponse.setIdentifier(noteIdentifier);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(notesReadService.getNoteByIdentifier(noteIdentifier)).thenReturn(notesResponse);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadNoteWriteService.deleteLeadNote(leadIdentifier, noteIdentifier);

        // Assert
        ArgumentCaptor<SystemEvent<?>> eventCaptor = ArgumentCaptor.forClass(SystemEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        assertEquals("LEAD_NOTE_DELETED", eventCaptor.getValue().getEventType(),
                "Event type should be LEAD_NOTE_DELETED");
    }
}
