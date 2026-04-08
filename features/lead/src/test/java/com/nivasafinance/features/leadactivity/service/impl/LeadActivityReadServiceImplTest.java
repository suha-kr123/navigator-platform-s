package com.nivasafinance.features.leadactivity.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lead.dto.LeadBasicResponse;
import com.nivasafinance.features.lead.service.LeadReadService;
import com.nivasafinance.features.leadactivity.dto.LeadActivityResponse;
import com.nivasafinance.features.leadactivity.enums.ResourceAction;
import com.nivasafinance.features.leadactivity.enums.ResourceEnum;
import com.nivasafinance.features.leadactivity.repository.LeadActivityRepositoryWrapper;
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
class LeadActivityReadServiceImplTest {

    @Mock
    private LeadActivityRepositoryWrapper leadActivityRepositoryWrapper;

    @Mock
    private LeadReadService leadReadService;

    @InjectMocks
    private LeadActivityReadServiceImpl leadActivityReadService;

    private UUID leadIdentifier;
    private Long leadId;

    @BeforeEach
    void setUp() {
        leadIdentifier = UUID.randomUUID();
        leadId = 1L;
    }

    // ==================== getActivities() Tests ====================

    @Test
    void getActivities_withValidRequest_resolvesLeadIdAndReturnsPaginatedResponse() {
        // Arrange
        PaginationRequest paginationRequest = new PaginationRequest();
        LeadBasicResponse leadBasic = LeadBasicResponse.builder().id(leadId).leadIdentifier(leadIdentifier).build();
        LeadActivityResponse activity = LeadActivityResponse.builder()
                .identifier(UUID.randomUUID())
                .resource(ResourceEnum.LEAD)
                .action(ResourceAction.CREATE)
                .description("Lead created")
                .build();
        PaginatedResponse<LeadActivityResponse> expected = PaginatedResponse.<LeadActivityResponse>builder()
                .content(List.of(activity))
                .pagination(PaginationInfo.builder().totalElements(1L).build())
                .build();

        when(leadReadService.getLeadBasicByIdentifier(leadIdentifier)).thenReturn(leadBasic);
        when(leadActivityRepositoryWrapper.findAllByLeadIdentifierWithException(leadId, paginationRequest))
                .thenReturn(expected);

        // Act
        PaginatedResponse<LeadActivityResponse> result = leadActivityReadService.getActivities(
                leadIdentifier, paginationRequest);

        // Assert
        assertNotNull(result, "Response should not be null");
        assertEquals(1L, result.getPagination().getTotalElements(), "Total elements should match");
        assertEquals(1, result.getContent().size(), "Content list should contain one activity");
        verify(leadReadService).getLeadBasicByIdentifier(leadIdentifier);
        verify(leadActivityRepositoryWrapper).findAllByLeadIdentifierWithException(leadId, paginationRequest);
    }

    @Test
    void getActivities_withEmptyResult_returnsEmptyPaginatedResponse() {
        // Arrange
        PaginationRequest paginationRequest = new PaginationRequest();
        LeadBasicResponse leadBasic = LeadBasicResponse.builder().id(leadId).leadIdentifier(leadIdentifier).build();
        PaginatedResponse<LeadActivityResponse> expected = PaginatedResponse.<LeadActivityResponse>builder()
                .content(List.of())
                .pagination(PaginationInfo.builder().totalElements(0L).build())
                .build();

        when(leadReadService.getLeadBasicByIdentifier(leadIdentifier)).thenReturn(leadBasic);
        when(leadActivityRepositoryWrapper.findAllByLeadIdentifierWithException(leadId, paginationRequest))
                .thenReturn(expected);

        // Act
        PaginatedResponse<LeadActivityResponse> result = leadActivityReadService.getActivities(
                leadIdentifier, paginationRequest);

        // Assert
        assertNotNull(result, "Response should not be null even for empty results");
        assertEquals(0L, result.getPagination().getTotalElements(), "Total elements should be zero");
        assertTrue(result.getContent().isEmpty(), "Content list should be empty");
    }
}
