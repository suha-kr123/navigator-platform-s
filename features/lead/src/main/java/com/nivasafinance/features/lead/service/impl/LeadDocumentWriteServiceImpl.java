package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.features.document.dto.DocumentCreateRequest;
import com.nivasafinance.features.document.dto.DocumentCreateResponse;
import com.nivasafinance.features.document.dto.DocumentResponse;
import com.nivasafinance.features.document.service.DocumentReadService;
import com.nivasafinance.features.document.service.DocumentWriteService;
import com.nivasafinance.features.lead.dto.LeadDocumentCreateRequest;
import com.nivasafinance.features.lead.dto.LeadDocumentCreateResponse;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.exception.VerifiedDocumentDeletionException;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadDocumentWriteService;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.nivasafinance.features.lead.utils.LeadDocumentUtils.generateDocumentPathForLead;

@Service
@Transactional
@AllArgsConstructor
public class LeadDocumentWriteServiceImpl implements LeadDocumentWriteService {

    private final DocumentWriteService documentWriteService;
    private final DocumentReadService documentReadService;
    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final MessageSource messageSource;

    @Override
    public LeadDocumentCreateResponse createLeadDocument(UUID leadIdentifier, MultipartFile file, LeadDocumentCreateRequest request) {
        // Find the lead
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);

        // Create document request with custom path for leads
        DocumentCreateRequest documentRequest = new DocumentCreateRequest(
                request.getName(),
                file,
                generateDocumentPathForLead(leadIdentifier, request.getName())
        );

        // Create the document
        DocumentCreateResponse documentResponse = documentWriteService.createDocument(documentRequest);

        // Create DocumentDetail for the lead
        Lead.DocumentDetail documentDetail = Lead.DocumentDetail.builder()
                .id(documentResponse.getId())
                .tag(request.getTags())
                .build();

        // Add to lead's document details list
        List<Lead.DocumentDetail> documentDetails = lead.getDocumentDetails();
        if (documentDetails == null) {
            documentDetails = new ArrayList<>();
            lead.setDocumentDetails(documentDetails);
        }
        documentDetails.add(documentDetail);

        // Save the lead
        leadRepositoryWrapper.saveWithException(lead);

        // Return response with document identifier
        return LeadDocumentCreateResponse.builder()
                .documentIdentifier(documentResponse.getIdentifier())
                .build();
    }

    @Override
    public void deleteLeadDocument(UUID leadIdentifier, UUID documentIdentifier) {
        // Find the lead
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);

        // Find the document by identifier
        DocumentResponse document = documentReadService.getDocumentByIdentifier(documentIdentifier);

        // Check if document is verified
        List<Lead.DocumentDetail> documentDetails = lead.getDocumentDetails();
        if (documentDetails != null) {
            // Remove from lead's document details
            documentDetails.removeIf(detail -> detail.getId().equals(document.getId()));
            lead.setDocumentDetails(documentDetails);
            leadRepositoryWrapper.saveWithException(lead);
        }

        // Delete the document
        documentWriteService.deleteDocumentById(documentIdentifier);
    }
}

