package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.events.BusinessEvent;
import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.common.events.payload.AdvisorNoteCreationEventPayload;
import com.nivasafinance.common.events.payload.AdvisorNoteDeletionEventPayload;
import com.nivasafinance.common.events.payload.AdvisorNoteUpdationEventPayload;
import com.nivasafinance.features.advisor.dto.AdvisorNoteCreateRequest;
import com.nivasafinance.features.advisor.dto.AdvisorNoteCreateResponse;
import com.nivasafinance.features.advisor.dto.AdvisorNoteUpdateRequest;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.advisor.service.AdvisorNoteWriteService;
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
public class AdvisorNoteWriteServiceImpl implements AdvisorNoteWriteService {

    private final NotesWriteService notesWriteService;
    private final NotesReadService notesReadService;
    private final AdvisorRepositoryWrapper advisorRepositoryWrapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public AdvisorNoteCreateResponse createAdvisorNote(UUID advisorIdentifier, AdvisorNoteCreateRequest request) {
        // Find the advisor
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier);

        // Create notes request
        NotesRequest notesRequest = new NotesRequest(
                request.getTitle(),
                request.getContent()
        );

        // Create the note
        NotesResponse notesResponse = notesWriteService.createNote(notesRequest);

        // Add to advisor's notes list
        List<Long> notes = advisor.getNotes();
        if (notes == null) {
            notes = new ArrayList<>();
            advisor.setNotes(notes);
        }
        notes.add(notesResponse.getId());

        // Save the advisor
        advisorRepositoryWrapper.saveWithException(advisor);

        // Publish event
        publishAdvisorNoteCreatedEvent(advisor, notesResponse);

        // Return response with note identifier
        return AdvisorNoteCreateResponse.builder()
                .noteIdentifier(notesResponse.getIdentifier())
                .build();
    }

    @Override
    public void updateAdvisorNote(UUID advisorIdentifier, UUID noteIdentifier, AdvisorNoteUpdateRequest request) {
        // Find the advisor
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier);

        // Get the note to verify it exists and belongs to the advisor
        NotesResponse note = notesReadService.getNoteByIdentifier(noteIdentifier);

        // Verify note belongs to advisor
        List<Long> notes = advisor.getNotes();
        if (notes == null || !notes.contains(note.getId())) {
            throw new RuntimeException("Note not found for advisor: " + advisorIdentifier +
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
        publishAdvisorNoteUpdatedEvent(advisor, note, noteIdentifier);
    }

    @Override
    public void deleteAdvisorNote(UUID advisorIdentifier, UUID noteIdentifier) {
        // Find the advisor
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier);

        // Get the note to verify it exists
        NotesResponse note = notesReadService.getNoteByIdentifier(noteIdentifier);

        // Verify note belongs to advisor
        List<Long> notes = advisor.getNotes();
        if (notes != null) {
            if (!notes.contains(note.getId())) {
                throw new RuntimeException("Note not found for advisor: " + advisorIdentifier +
                                           " with note identifier: " + noteIdentifier);
            }

            // Remove from advisor's notes list
            notes.remove(note.getId());
            advisor.setNotes(notes);
            advisorRepositoryWrapper.saveWithException(advisor);
        }

        // Publish event before deleting
        publishAdvisorNoteDeletedEvent(advisor, note, noteIdentifier);

        // Delete the note
        notesWriteService.deleteNote(noteIdentifier);
    }

    private void publishAdvisorNoteCreatedEvent(Advisor advisor, NotesResponse notesResponse) {
        AdvisorNoteCreationEventPayload payload = AdvisorNoteCreationEventPayload.builder()
                .advisorId(advisor.getId())
                .noteId(notesResponse.getId())
                .noteIdentifier(notesResponse.getIdentifier())
                .build();

        String username = UserContext.getUsername();
        applicationEventPublisher.publishEvent(
                new SystemEvent<>(BusinessEvent.ADVISOR_NOTE_CREATED.toString(), payload, username)
        );
    }

    private void publishAdvisorNoteUpdatedEvent(Advisor advisor, NotesResponse note, UUID noteIdentifier) {
        AdvisorNoteUpdationEventPayload payload = AdvisorNoteUpdationEventPayload.builder()
                .advisorId(advisor.getId())
                .noteId(note.getId())
                .noteIdentifier(noteIdentifier)
                .build();

        String username = UserContext.getUsername();
        applicationEventPublisher.publishEvent(
                new SystemEvent<>(BusinessEvent.ADVISOR_NOTE_UPDATED.toString(), payload, username)
        );
    }

    private void publishAdvisorNoteDeletedEvent(Advisor advisor, NotesResponse note, UUID noteIdentifier) {
        AdvisorNoteDeletionEventPayload payload = AdvisorNoteDeletionEventPayload.builder()
                .advisorId(advisor.getId())
                .noteId(note.getId())
                .noteIdentifier(noteIdentifier)
                .build();

        String username = UserContext.getUsername();
        applicationEventPublisher.publishEvent(
                new SystemEvent<>(BusinessEvent.ADVISOR_NOTE_DELETED.toString(), payload, username)
        );
    }
}

