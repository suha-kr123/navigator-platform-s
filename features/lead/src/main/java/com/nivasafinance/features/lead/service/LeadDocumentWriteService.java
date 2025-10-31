package com.nivasafinance.features.lead.service;

import com.nivasafinance.features.lead.dto.LeadDocumentCreateRequest;
import com.nivasafinance.features.lead.dto.LeadDocumentCreateResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Service interface for write operations on lead documents.
 * Handles all operations that modify lead document state.
 */
public interface LeadDocumentWriteService {

    /**
     * Creates a new document for a lead.
     * @param leadIdentifier The lead identifier
     * @param request The document creation request
     * @return The created document response
     */
    LeadDocumentCreateResponse createLeadDocument(UUID leadIdentifier, MultipartFile file, LeadDocumentCreateRequest request);

    /**
     * Deletes a document from a lead.
     * @param leadIdentifier The lead identifier
     * @param documentIdentifier The document identifier
     */
    void deleteLeadDocument(UUID leadIdentifier, UUID documentIdentifier);
}

