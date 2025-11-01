package com.nivasafinance.features.lead.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lead.dto.LeadNoteResponse;

import java.util.UUID;

/**
 * Service interface for read operations on lead notes.
 * Handles all query operations that don't modify lead note state.
 */
public interface LeadNoteReadService {

    /**
     * Retrieves all notes for a lead with pagination.
     * @param leadIdentifier The lead identifier
     * @param paginationRequest The pagination request containing offset, limit, sortBy, and sortDirection
     * @return Paginated response of lead notes
     */
    PaginatedResponse<LeadNoteResponse> getAllLeadNotes(
            UUID leadIdentifier, PaginationRequest paginationRequest);

    /**
     * Retrieves a specific note for a lead.
     * @param leadIdentifier The lead identifier
     * @param noteIdentifier The note identifier
     * @return The lead note response
     */
    LeadNoteResponse getLeadNoteById(UUID leadIdentifier, UUID noteIdentifier);
}

