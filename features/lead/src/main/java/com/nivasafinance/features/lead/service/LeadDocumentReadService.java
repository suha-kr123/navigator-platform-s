package com.nivasafinance.features.lead.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lead.dto.LeadDocumentResponse;

import java.util.UUID;

/**
 * Service interface for read operations on lead documents.
 * Handles all query operations that don't modify lead document state.
 */
public interface LeadDocumentReadService {

    /**
     * Retrieves all documents for a lead with pagination.
     * @param leadIdentifier The lead identifier
     * @param paginationRequest The pagination request containing offset, limit, sortBy, and sortDirection
     * @return Paginated response of lead documents
     */
    PaginatedResponse<LeadDocumentResponse> getAllLeadDocuments(
            UUID leadIdentifier, PaginationRequest paginationRequest);

    /**
     * Retrieves a specific document for a lead.
     * @param leadIdentifier The lead identifier
     * @param documentIdentifier The document identifier
     * @return The lead document response
     */
    LeadDocumentResponse getLeadDocumentById(UUID leadIdentifier, UUID documentIdentifier);
}

