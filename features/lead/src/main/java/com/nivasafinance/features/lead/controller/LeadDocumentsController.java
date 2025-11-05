package com.nivasafinance.features.lead.controller;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.lead.dto.HouseFrontPhotoRequest;
import com.nivasafinance.features.lead.dto.LeadDocumentCreateRequest;
import com.nivasafinance.features.lead.dto.LeadDocumentCreateResponse;
import com.nivasafinance.features.lead.dto.LeadDocumentResponse;
import com.nivasafinance.features.lead.service.LeadDocumentReadService;
import com.nivasafinance.features.lead.service.LeadDocumentWriteService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.V1 + "/leads/{leadId}/documents")
@AllArgsConstructor
public class LeadDocumentsController {

    private final LeadDocumentWriteService leadDocumentWriteService;
    private final LeadDocumentReadService leadDocumentReadService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LeadDocumentCreateResponse> createLeadDocument(
            @PathVariable UUID leadId,
            @Valid @RequestPart("metadata") LeadDocumentCreateRequest request,
            @RequestPart("file") MultipartFile file
    ) {
        LeadDocumentCreateResponse response = leadDocumentWriteService.createLeadDocument(leadId,file, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/front-house-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LeadDocumentCreateResponse> createHouseFrontPhoto(
            @PathVariable UUID leadId,
            @Valid @RequestPart("metadata") HouseFrontPhotoRequest request,
            @RequestPart("file") MultipartFile file
    ) {
        LeadDocumentCreateResponse response = leadDocumentWriteService.createHouseFrontPhoto(leadId, file, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<LeadDocumentResponse>> getAllLeadDocuments(
            @PathVariable UUID leadId,
            @Valid PaginationRequest paginationRequest) {
        
        PaginatedResponse<LeadDocumentResponse> documents = leadDocumentReadService.getAllLeadDocuments(
                leadId, paginationRequest);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<LeadDocumentResponse> getLeadDocumentById(
            @PathVariable UUID leadId,
            @PathVariable UUID documentId) {
        LeadDocumentResponse document = leadDocumentReadService.getLeadDocumentById(leadId, documentId);
        return ResponseEntity.ok(document);
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteLeadDocument(
            @PathVariable UUID leadId,
            @PathVariable UUID documentId) {
        leadDocumentWriteService.deleteLeadDocument(leadId, documentId);
        return ResponseEntity.noContent().build();
    }
}
