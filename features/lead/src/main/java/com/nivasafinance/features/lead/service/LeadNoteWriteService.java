package com.nivasafinance.features.lead.service;

import com.nivasafinance.features.lead.dto.LeadNoteCreateRequest;
import com.nivasafinance.features.lead.dto.LeadNoteCreateResponse;
import com.nivasafinance.features.lead.dto.LeadNoteResponse;
import com.nivasafinance.features.lead.dto.LeadNoteUpdateRequest;

import java.util.UUID;

/**
 * Service interface for write operations on lead notes.
 * Handles all operations that modify lead note state.
 */
public interface LeadNoteWriteService {

    /**
     * Creates a new note for a lead.
     * @param leadIdentifier The lead identifier
     * @param request The note creation request
     * @return The created note response
     */
    LeadNoteCreateResponse createLeadNote(UUID leadIdentifier, LeadNoteCreateRequest request);

    /**
     * Updates an existing note for a lead.
     * @param leadIdentifier The lead identifier
     * @param noteIdentifier The note identifier
     * @param request The note update request
     */
    void updateLeadNote(UUID leadIdentifier, UUID noteIdentifier, LeadNoteUpdateRequest request);

    /**
     * Deletes a note from a lead.
     * @param leadIdentifier The lead identifier
     * @param noteIdentifier The note identifier
     */
    void deleteLeadNote(UUID leadIdentifier, UUID noteIdentifier);
}

