package com.nivasafinance.features.lead.service;

import com.nivasafinance.features.lead.dto.HouseFrontPhotoRequest;
import com.nivasafinance.features.lead.dto.LeadDocumentCreateRequest;
import com.nivasafinance.features.lead.dto.LeadDocumentCreateResponse;
import com.nivasafinance.features.lead.dto.LeadDocumentUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Service interface for write operations on lead documents.
 * Handles all operations that modify lead document state.
 */
public interface LeadDocumentWriteService {

    /**
     * Creates a new document for a lead from an uploaded file.
     * @param leadIdentifier The lead identifier
     * @param file The uploaded file
     * @param request The document creation request
     * @return The created document response
     */
    LeadDocumentCreateResponse createLeadDocument(UUID leadIdentifier, MultipartFile file, LeadDocumentCreateRequest request);

    /**
     * Creates a new document for a lead from in-memory content (e.g. from Redash).
     * @param leadIdentifier The lead identifier
     * @param content The document content
     * @param filename The filename (e.g. cb-report.xlsx)
     * @param contentType The content type (e.g. application/vnd.openxmlformats-officedocument.spreadsheetml.sheet)
     * @param request The document creation request
     * @return The created document response
     */
    LeadDocumentCreateResponse createLeadDocument(UUID leadIdentifier, byte[] content, String filename, String contentType, LeadDocumentCreateRequest request);

    /**
     * Creates a house front photo for a lead with geolocation data.
     * @param leadIdentifier The lead identifier
     * @param file The photo file (JPG/JPEG/PNG only)
     * @param request The house front photo request containing GeoData
     * @return The created document response
     */
    LeadDocumentCreateResponse createHouseFrontPhoto(UUID leadIdentifier, MultipartFile file, HouseFrontPhotoRequest request);

    /**
     * Deletes a document from a lead.
     * @param leadIdentifier The lead identifier
     * @param documentIdentifier The document identifier
     */
    void deleteLeadDocument(UUID leadIdentifier, UUID documentIdentifier);

    /**
     * Updates tags associated with a lead document.
     * @param leadIdentifier The lead identifier
     * @param documentIdentifier The document identifier
     * @param request The request containing tags that need to be updated
     */
    void updateLeadDocument(UUID leadIdentifier, UUID documentIdentifier, LeadDocumentUpdateRequest request);
}

