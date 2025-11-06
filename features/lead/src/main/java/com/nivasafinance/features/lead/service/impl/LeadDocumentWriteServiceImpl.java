package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.features.document.dto.DocumentCreateRequest;
import com.nivasafinance.features.document.dto.DocumentCreateResponse;
import com.nivasafinance.features.document.dto.DocumentResponse;
import com.nivasafinance.features.document.service.DocumentReadService;
import com.nivasafinance.features.document.service.DocumentWriteService;
import com.nivasafinance.features.lead.dto.HouseFrontPhotoRequest;
import com.nivasafinance.features.lead.dto.LeadDocumentCreateRequest;
import com.nivasafinance.features.lead.dto.LeadDocumentCreateResponse;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadDocumentWriteService;
import com.nivasafinance.features.master.codemaster.SystemLeadDocumentsMaster;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.nivasafinance.common.utils.DocumentUtils.getFileExtension;
import static com.nivasafinance.features.lead.utils.LeadDocumentUtils.generateDocumentPathForLead;

@Service
@Transactional
@AllArgsConstructor
public class LeadDocumentWriteServiceImpl implements LeadDocumentWriteService {

    private final DocumentWriteService documentWriteService;
    private final DocumentReadService documentReadService;
    private final LeadRepositoryWrapper leadRepositoryWrapper;
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
    public LeadDocumentCreateResponse createHouseFrontPhoto(UUID leadIdentifier, MultipartFile file, HouseFrontPhotoRequest request) {
        // Validate file type (JPG/JPEG/PNG only)
        String contentType = file.getContentType();
        if (contentType == null || 
            (!contentType.equals("image/jpeg") && !contentType.equals("image/jpg") && !contentType.equals("image/png"))) {
            throw new BadRequestException("Only JPG, JPEG, and PNG image formats are allowed");
        }

        // Find the lead
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);

        // Get file extension from original filename or content type
        String fileExtension = getFileExtension(file).orElseThrow(() -> new BadRequestException("Bad File uploaded"));
        String documentName = "house-photo" + fileExtension;

        // Create document request with custom path for leads
        DocumentCreateRequest documentRequest = new DocumentCreateRequest(
                documentName,
                file,
                generateDocumentPathForLead(leadIdentifier, documentName)
        );

        // Create the document
        DocumentCreateResponse documentResponse = documentWriteService.createDocument(documentRequest);

        // Create DocumentDetail for the lead with HOUSE_FRONT tag
        Lead.DocumentDetail documentDetail = Lead.DocumentDetail.builder()
                .id(documentResponse.getId())
                .tag(List.of(SystemLeadDocumentsMaster.LeadPropertyDocuments.HOUSE_FRONT))
                .build();

        // Add to lead's document details list
        List<Lead.DocumentDetail> documentDetails = lead.getDocumentDetails();
        if (documentDetails == null) {
            documentDetails = new ArrayList<>();
            lead.setDocumentDetails(documentDetails);
        }
        documentDetails.add(documentDetail);

        // Update property details with GeoData
        Lead.OtherDetails otherDetails = lead.getOtherDetails();
        if (otherDetails == null) {
            otherDetails = Lead.OtherDetails.builder().build();
            lead.setOtherDetails(otherDetails);
        }
        
        Lead.PropertyDetails propertyDetails = otherDetails.getPropertyDetails();
        if (propertyDetails == null) {
            propertyDetails = Lead.PropertyDetails.builder().build();
            otherDetails.setPropertyDetails(propertyDetails);
        }
        
        propertyDetails.setGeoData(request.getGeoData());

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

