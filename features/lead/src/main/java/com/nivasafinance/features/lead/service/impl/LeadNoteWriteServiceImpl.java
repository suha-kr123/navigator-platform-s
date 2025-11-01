package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.features.lead.dto.LeadNoteCreateRequest;
import com.nivasafinance.features.lead.dto.LeadNoteCreateResponse;
import com.nivasafinance.features.lead.dto.LeadNoteResponse;
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

        // Delete the note
        notesWriteService.deleteNote(noteIdentifier);
    }
}

