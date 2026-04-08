package com.nivasafinance.features.leadactivity.service.impl;

import com.nivasafinance.features.leadactivity.dto.CreateLeadActivityRequest;
import com.nivasafinance.features.leadactivity.dto.CreateLeadActivityResponse;
import com.nivasafinance.features.leadactivity.entity.LeadActivity;
import com.nivasafinance.features.leadactivity.enums.ResourceAction;
import com.nivasafinance.features.leadactivity.enums.ResourceEnum;
import com.nivasafinance.features.leadactivity.repository.LeadActivityRepositoryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadActivityWriteServiceImplTest {

    @Mock
    private LeadActivityRepositoryWrapper leadActivityRepositoryWrapper;

    @InjectMocks
    private LeadActivityWriteServiceImpl leadActivityWriteService;

    // ==================== createLeadActivity() Tests ====================

    @Test
    void createLeadActivity_withValidRequest_savesAndReturnsResponse() {
        // Arrange
        CreateLeadActivityRequest request = CreateLeadActivityRequest.builder()
                .leadId(1L)
                .resource(ResourceEnum.LEAD)
                .action(ResourceAction.CREATE)
                .description("Lead created")
                .metadata(Map.of("key", "value"))
                .resourceId(10L)
                .createdBy("test-user")
                .build();

        UUID savedIdentifier = UUID.randomUUID();
        LeadActivity savedActivity = LeadActivity.builder()
                .id(100L)
                .identifier(savedIdentifier)
                .leadId(1L)
                .build();

        when(leadActivityRepositoryWrapper.saveWithException(any(LeadActivity.class))).thenReturn(savedActivity);

        // Act
        CreateLeadActivityResponse result = leadActivityWriteService.createLeadActivity(request);

        // Assert
        assertNotNull(result, "Response should not be null");
        assertEquals(100L, result.getId(), "ID should match the saved activity");
        assertEquals(savedIdentifier, result.getIdentifier(), "Identifier should match the saved activity");
        verify(leadActivityRepositoryWrapper).saveWithException(any(LeadActivity.class));
    }

    @Test
    void createLeadActivity_withValidRequest_mapsAllFieldsToEntity() {
        // Arrange
        Map<String, Object> metadata = Map.of("status", "ACTIVE");
        CreateLeadActivityRequest request = CreateLeadActivityRequest.builder()
                .leadId(5L)
                .resource(ResourceEnum.NOTES)
                .action(ResourceAction.UPDATE)
                .description("Note updated")
                .metadata(metadata)
                .resourceId(20L)
                .createdBy("admin-user")
                .build();

        LeadActivity savedActivity = LeadActivity.builder()
                .id(200L).identifier(UUID.randomUUID()).build();
        when(leadActivityRepositoryWrapper.saveWithException(any(LeadActivity.class))).thenReturn(savedActivity);

        // Act
        leadActivityWriteService.createLeadActivity(request);

        // Assert
        ArgumentCaptor<LeadActivity> captor = ArgumentCaptor.forClass(LeadActivity.class);
        verify(leadActivityRepositoryWrapper).saveWithException(captor.capture());
        LeadActivity captured = captor.getValue();
        assertEquals(5L, captured.getLeadId(), "Lead ID should be mapped from request");
        assertEquals(ResourceEnum.NOTES, captured.getResourceType(), "Resource type should be mapped from request");
        assertEquals(ResourceAction.UPDATE, captured.getResourceAction(), "Resource action should be mapped from request");
        assertEquals("Note updated", captured.getDescription(), "Description should be mapped from request");
        assertEquals(metadata, captured.getMetadata(), "Metadata should be mapped from request");
        assertEquals(20L, captured.getResourceId(), "Resource ID should be mapped from request");
        assertEquals("admin-user", captured.getCreatedBy(), "CreatedBy should be explicitly set from request");
        assertNotNull(captured.getIdentifier(), "Identifier should be auto-generated");
    }

    @Test
    void createLeadActivity_withNullCreatedBy_doesNotOverrideCreatedBy() {
        // Arrange
        CreateLeadActivityRequest request = CreateLeadActivityRequest.builder()
                .leadId(1L)
                .resource(ResourceEnum.DOCUMENTS)
                .action(ResourceAction.DELETE)
                .description("Document deleted")
                .createdBy(null)
                .build();

        LeadActivity savedActivity = LeadActivity.builder()
                .id(300L).identifier(UUID.randomUUID()).build();
        when(leadActivityRepositoryWrapper.saveWithException(any(LeadActivity.class))).thenReturn(savedActivity);

        // Act
        leadActivityWriteService.createLeadActivity(request);

        // Assert
        ArgumentCaptor<LeadActivity> captor = ArgumentCaptor.forClass(LeadActivity.class);
        verify(leadActivityRepositoryWrapper).saveWithException(captor.capture());
        assertNull(captor.getValue().getCreatedBy(),
                "CreatedBy should not be set when request createdBy is null (falls back to JPA auditing)");
    }
}
