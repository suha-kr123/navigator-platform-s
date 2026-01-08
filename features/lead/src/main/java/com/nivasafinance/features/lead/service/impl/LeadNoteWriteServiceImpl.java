package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.events.BusinessEvent;
import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.common.events.payload.LeadNoteCreationEventPayload;
import com.nivasafinance.common.events.payload.LeadNoteDeletionEventPayload;
import com.nivasafinance.common.events.payload.LeadNoteUpdationEventPayload;
import com.nivasafinance.features.lead.dto.LeadNoteCreateRequest;
import com.nivasafinance.features.lead.dto.LeadNoteCreateResponse;
import com.nivasafinance.features.lead.dto.LeadNoteUpdateRequest;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadNoteWriteService;
import com.nivasafinance.features.notes.dto.NotesRequest;
import com.nivasafinance.features.notes.dto.NotesResponse;
import com.nivasafinance.features.notes.dto.NotesUpdateRequest;
import com.nivasafinance.features.notes.service.NotesReadService;
import com.nivasafinance.features.notes.service.NotesWriteService;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
public class LeadNoteWriteServiceImpl implements LeadNoteWriteService {

    private final NotesWriteService notesWriteService;
    private final NotesReadService notesReadService;
    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public LeadNoteCreateResponse createLeadNote(UUID leadIdentifier, LeadNoteCreateRequest request) {
        // Find the lead
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);

        // Create notes request
        NotesRequest notesRequest = new NotesRequest(
                request.getTitle(),
                request.getContent()
        );

        // Create the note
        NotesResponse notesResponse = notesWriteService.createNote(notesRequest);

        // Add to lead's notes list
        List<Long> notes = lead.getNotes();
        if (notes == null) {
            notes = new ArrayList<>();
            lead.setNotes(notes);
        }
        notes.add(notesResponse.getId());

        // Save the lead
        leadRepositoryWrapper.saveWithException(lead);

        // Publish event
        publishLeadNoteCreatedEvent(lead, notesResponse);

        // Return response with note identifier
        return LeadNoteCreateResponse.builder()
                .noteIdentifier(notesResponse.getIdentifier())
                .build();
    }

    @Override
    public void updateLeadNote(UUID leadIdentifier, UUID noteIdentifier, LeadNoteUpdateRequest request) {
        // Find the lead
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);

        // Get the note to verify it exists and belongs to the lead
        NotesResponse note = notesReadService.getNoteByIdentifier(noteIdentifier);

        // Verify note belongs to lead
        List<Long> notes = lead.getNotes();
        if (notes == null || !notes.contains(note.getId())) {
            throw new RuntimeException("Note not found for lead: " + leadIdentifier +
                                       " with note identifier: " + noteIdentifier);
        }

        // Update notes request
        NotesUpdateRequest notesUpdateRequest = new NotesUpdateRequest(
                request.getTitle(),
                request.getContent()
        );

        // Update the note
        notesWriteService.updateNote(noteIdentifier, notesUpdateRequest);

        // Publish event
        publishLeadNoteUpdatedEvent(lead, note, noteIdentifier);
    }

    @Override
    public void deleteLeadNote(UUID leadIdentifier, UUID noteIdentifier) {
        // Find the lead
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);

        // Get the note to verify it exists
        NotesResponse note = notesReadService.getNoteByIdentifier(noteIdentifier);

        // Verify note belongs to lead
        List<Long> notes = lead.getNotes();
        if (notes != null) {
            if (!notes.contains(note.getId())) {
                throw new RuntimeException("Note not found for lead: " + leadIdentifier +
                                           " with note identifier: " + noteIdentifier);
            }

            // Remove from lead's notes list
            notes.remove(note.getId());
            lead.setNotes(notes);
            leadRepositoryWrapper.saveWithException(lead);
        }

        // Publish event before deleting
        publishLeadNoteDeletedEvent(lead, note, noteIdentifier);

        // Delete the note
        notesWriteService.deleteNote(noteIdentifier);
    }

    private void publishLeadNoteCreatedEvent(Lead lead, NotesResponse notesResponse) {
        LeadNoteCreationEventPayload payload = LeadNoteCreationEventPayload.builder()
                .leadId(lead.getId())
                .noteId(notesResponse.getId())
                .noteIdentifier(notesResponse.getIdentifier())
                .build();

        String username = UserContext.getUsername();
        applicationEventPublisher.publishEvent(
                new SystemEvent<>(BusinessEvent.LEAD_NOTE_CREATED.toString(), payload, username)
        );
    }

    private void publishLeadNoteUpdatedEvent(Lead lead, NotesResponse note, UUID noteIdentifier) {
        LeadNoteUpdationEventPayload payload = LeadNoteUpdationEventPayload.builder()
                .leadId(lead.getId())
                .noteId(note.getId())
                .noteIdentifier(noteIdentifier)
                .build();

        String username = UserContext.getUsername();
        applicationEventPublisher.publishEvent(
                new SystemEvent<>(BusinessEvent.LEAD_NOTE_UPDATED.toString(), payload, username)
        );
    }

    private void publishLeadNoteDeletedEvent(Lead lead, NotesResponse note, UUID noteIdentifier) {
        LeadNoteDeletionEventPayload payload = LeadNoteDeletionEventPayload.builder()
                .leadId(lead.getId())
                .noteId(note.getId())
                .noteIdentifier(noteIdentifier)
                .build();

        String username = UserContext.getUsername();
        applicationEventPublisher.publishEvent(
                new SystemEvent<>(BusinessEvent.LEAD_NOTE_DELETED.toString(), payload, username)
        );
    }
}

