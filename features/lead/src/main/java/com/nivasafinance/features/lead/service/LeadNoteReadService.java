package com.nivasafinance.features.lead.service;

import com.nivasafinance.features.lead.dto.LeadNoteResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for read operations on lead notes.
 * Handles all query operations that don't modify lead note state.
 */
public interface LeadNoteReadService {

    /**
     * Retrieves all notes for a lead.
     * @param leadIdentifier The lead identifier
     * @return List of lead notes
     */
    List<LeadNoteResponse> getAllLeadNotes(UUID leadIdentifier);

    /**
     * Retrieves a specific note for a lead.
     * @param leadIdentifier The lead identifier
     * @param noteIdentifier The note identifier
     * @return The lead note response
     */
    LeadNoteResponse getLeadNoteById(UUID leadIdentifier, UUID noteIdentifier);
}

