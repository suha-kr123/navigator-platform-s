package com.nivasafinance.features.lead.service;

import com.nivasafinance.features.lead.dto.LeadDocumentResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for read operations on lead documents.
 * Handles all query operations that don't modify lead document state.
 */
public interface LeadDocumentReadService {

    /**
     * Retrieves all documents for a lead.
     * @param leadIdentifier The lead identifier
     * @return List of lead documents
     */
    List<LeadDocumentResponse> getAllLeadDocuments(UUID leadIdentifier);

    /**
     * Retrieves a specific document for a lead.
     * @param leadIdentifier The lead identifier
     * @param documentIdentifier The document identifier
     * @return The lead document response
     */
    LeadDocumentResponse getLeadDocumentById(UUID leadIdentifier, UUID documentIdentifier);
}

