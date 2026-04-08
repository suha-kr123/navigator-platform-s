package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lead.dto.LeadNoteResponse;
import com.nivasafinance.features.lead.repository.LeadNoteRepositoryWrapper;
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
class LeadNoteReadServiceImplTest {

    @Mock
    private LeadNoteRepositoryWrapper leadNoteRepositoryWrapper;

    @InjectMocks
    private LeadNoteReadServiceImpl leadNoteReadService;

    private UUID leadIdentifier;
    private UUID noteIdentifier;

    @BeforeEach
    void setUp() {
        leadIdentifier = UUID.randomUUID();
        noteIdentifier = UUID.randomUUID();
    }

    // ==================== getAllLeadNotes() Tests ====================

    @Test
    void getAllLeadNotes_withValidRequest_returnsPaginatedResponse() {
        // Arrange
        PaginationRequest paginationRequest = new PaginationRequest();
        LeadNoteResponse note = LeadNoteResponse.builder()
                .identifier(noteIdentifier).title("Follow-up").content("Call scheduled").build();
        PaginationInfo paginationInfo = PaginationInfo.builder().totalElements(1L).build();
        PaginatedResponse<LeadNoteResponse> expected = PaginatedResponse.<LeadNoteResponse>builder()
                .content(List.of(note)).pagination(paginationInfo).build();

        when(leadNoteRepositoryWrapper.findAllNotesByLeadIdentifier(leadIdentifier, paginationRequest))
                .thenReturn(expected);

        // Act
        PaginatedResponse<LeadNoteResponse> result = leadNoteReadService.getAllLeadNotes(
                leadIdentifier, paginationRequest);

        // Assert
        assertNotNull(result, "Response should not be null");
        assertEquals(1L, result.getPagination().getTotalElements(), "Total elements should match");
        assertEquals(1, result.getContent().size(), "Content list should contain one note");
        verify(leadNoteRepositoryWrapper).findAllNotesByLeadIdentifier(leadIdentifier, paginationRequest);
    }

    @Test
    void getAllLeadNotes_withEmptyResult_returnsEmptyPaginatedResponse() {
        // Arrange
        PaginationRequest paginationRequest = new PaginationRequest();
        PaginationInfo paginationInfo = PaginationInfo.builder().totalElements(0L).build();
        PaginatedResponse<LeadNoteResponse> expected = PaginatedResponse.<LeadNoteResponse>builder()
                .content(List.of()).pagination(paginationInfo).build();

        when(leadNoteRepositoryWrapper.findAllNotesByLeadIdentifier(leadIdentifier, paginationRequest))
                .thenReturn(expected);

        // Act
        PaginatedResponse<LeadNoteResponse> result = leadNoteReadService.getAllLeadNotes(
                leadIdentifier, paginationRequest);

        // Assert
        assertNotNull(result, "Response should not be null even for empty results");
        assertEquals(0L, result.getPagination().getTotalElements(), "Total elements should be zero");
        assertTrue(result.getContent().isEmpty(), "Content list should be empty");
    }

    // ==================== getLeadNoteById() Tests ====================

    @Test
    void getLeadNoteById_withValidIdentifiers_returnsNote() {
        // Arrange
        LeadNoteResponse expected = LeadNoteResponse.builder()
                .identifier(noteIdentifier).title("Site visit").content("Completed").build();

        when(leadNoteRepositoryWrapper.findNoteByLeadIdentifierAndNoteIdentifier(
                leadIdentifier, noteIdentifier)).thenReturn(expected);

        // Act
        LeadNoteResponse result = leadNoteReadService.getLeadNoteById(leadIdentifier, noteIdentifier);

        // Assert
        assertNotNull(result, "Response should not be null");
        assertEquals(noteIdentifier, result.getIdentifier(), "Note identifier should match");
        assertEquals("Site visit", result.getTitle(), "Note title should match");
        verify(leadNoteRepositoryWrapper).findNoteByLeadIdentifierAndNoteIdentifier(
                leadIdentifier, noteIdentifier);
    }
}
