package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lead.dto.LeadDocumentResponse;
import com.nivasafinance.features.lead.repository.LeadDocumentRepositoryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadDocumentReadServiceImplTest {

    @Mock
    private LeadDocumentRepositoryWrapper leadDocumentRepositoryWrapper;

    @InjectMocks
    private LeadDocumentReadServiceImpl leadDocumentReadService;

    private UUID leadIdentifier;
    private UUID documentIdentifier;

    @BeforeEach
    void setUp() {
        leadIdentifier = UUID.randomUUID();
        documentIdentifier = UUID.randomUUID();
    }

    // ==================== getAllLeadDocuments() Tests ====================

    @Test
    void getAllLeadDocuments_withValidRequest_returnsPaginatedResponse() {
        // Arrange
        PaginationRequest paginationRequest = new PaginationRequest();
        LeadDocumentResponse doc = LeadDocumentResponse.builder()
                .identifier(documentIdentifier).name("doc.pdf").build();
        PaginationInfo paginationInfo = PaginationInfo.builder().totalElements(1L).build();
        PaginatedResponse<LeadDocumentResponse> expected = PaginatedResponse.<LeadDocumentResponse>builder()
                .content(List.of(doc)).pagination(paginationInfo).build();

        when(leadDocumentRepositoryWrapper.findAllDocumentsByLeadIdentifier(leadIdentifier, paginationRequest))
                .thenReturn(expected);

        // Act
        PaginatedResponse<LeadDocumentResponse> result = leadDocumentReadService.getAllLeadDocuments(
                leadIdentifier, paginationRequest);

        // Assert
        assertNotNull(result, "Response should not be null");
        assertEquals(1L, result.getPagination().getTotalElements(), "Total elements should match");
        assertEquals(1, result.getContent().size(), "Content list should contain one document");
        verify(leadDocumentRepositoryWrapper).findAllDocumentsByLeadIdentifier(leadIdentifier, paginationRequest);
    }

    @Test
    void getAllLeadDocuments_withEmptyResult_returnsEmptyPaginatedResponse() {
        // Arrange
        PaginationRequest paginationRequest = new PaginationRequest();
        PaginationInfo paginationInfo = PaginationInfo.builder().totalElements(0L).build();
        PaginatedResponse<LeadDocumentResponse> expected = PaginatedResponse.<LeadDocumentResponse>builder()
                .content(List.of()).pagination(paginationInfo).build();

        when(leadDocumentRepositoryWrapper.findAllDocumentsByLeadIdentifier(leadIdentifier, paginationRequest))
                .thenReturn(expected);

        // Act
        PaginatedResponse<LeadDocumentResponse> result = leadDocumentReadService.getAllLeadDocuments(
                leadIdentifier, paginationRequest);

        // Assert
        assertNotNull(result, "Response should not be null even for empty results");
        assertEquals(0L, result.getPagination().getTotalElements(), "Total elements should be zero");
        assertTrue(result.getContent().isEmpty(), "Content list should be empty");
    }

    // ==================== getLeadDocumentById() Tests ====================

    @Test
    void getLeadDocumentById_withValidIdentifiers_returnsDocument() {
        // Arrange
        LeadDocumentResponse expected = LeadDocumentResponse.builder()
                .identifier(documentIdentifier).name("report.pdf").type("application/pdf").build();

        when(leadDocumentRepositoryWrapper.findDocumentByLeadIdentifierAndDocumentIdentifier(
                leadIdentifier, documentIdentifier)).thenReturn(expected);

        // Act
        LeadDocumentResponse result = leadDocumentReadService.getLeadDocumentById(leadIdentifier, documentIdentifier);

        // Assert
        assertNotNull(result, "Response should not be null");
        assertEquals(documentIdentifier, result.getIdentifier(), "Document identifier should match");
        assertEquals("report.pdf", result.getName(), "Document name should match");
        verify(leadDocumentRepositoryWrapper).findDocumentByLeadIdentifierAndDocumentIdentifier(
                leadIdentifier, documentIdentifier);
    }
}
