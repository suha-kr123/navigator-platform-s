package com.nivasafinance.features.lead.service;

import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.task.dto.TaskResponse;
import com.nivasafinance.features.task.service.TaskReadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadTaskTimelineServiceTest {

    @Mock
    private LeadRepositoryWrapper leadRepositoryWrapper;

    @Mock
    private TaskReadService taskReadService;

    @InjectMocks
    private LeadTaskTimelineService leadTaskTimelineService;

    private UUID leadIdentifier;
    private Lead lead;

    @BeforeEach
    void setUp() {
        leadIdentifier = UUID.randomUUID();
        lead = new Lead();
        lead.setId(1L);
        lead.setLeadIdentifier(leadIdentifier);
    }

    // ==================== refreshByLeadIdentifier() Tests ====================

    @Test
    void refreshByLeadIdentifier_withOpenTasks_computesAndSavesTimeline() {
        // Arrange
        TaskResponse openTask = TaskResponse.builder()
                .taskIdentifier(UUID.randomUUID())
                .taskConfigKey("SITE_VISIT")
                .taskName("Site Visit")
                .dueAt(LocalDateTime.now().plusDays(1))
                .createdAt(LocalDateTime.now())
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(taskReadService.findAllTasksForLead(leadIdentifier, true)).thenReturn(List.of(openTask));
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadTaskTimelineService.refreshByLeadIdentifier(leadIdentifier);

        // Assert
        assertNotNull(lead.getTaskTimeline(), "Task timeline should be computed and set on lead");
        assertNotNull(lead.getTaskTimeline().getNext(), "Next slot should be populated for an open task");
        verify(leadRepositoryWrapper).saveWithException(lead);
    }

    @Test
    void refreshByLeadIdentifier_withEmptyTasks_setsNullTimeline() {
        // Arrange
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(taskReadService.findAllTasksForLead(leadIdentifier, true)).thenReturn(Collections.emptyList());
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadTaskTimelineService.refreshByLeadIdentifier(leadIdentifier);

        // Assert
        assertNull(lead.getTaskTimeline(), "Task timeline should be null when no tasks exist");
        verify(leadRepositoryWrapper).saveWithException(lead);
    }

    @Test
    void refreshByLeadIdentifier_withCompletedAndOpenTasks_computesPreviousAndNext() {
        // Arrange
        TaskResponse completedTask = TaskResponse.builder()
                .taskIdentifier(UUID.randomUUID())
                .taskConfigKey("DOC_COLLECT")
                .taskName("Document Collection")
                .dueAt(LocalDateTime.now().minusDays(2))
                .createdAt(LocalDateTime.now().minusDays(3))
                .outcome("COMPLETED")
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();

        TaskResponse openTask = TaskResponse.builder()
                .taskIdentifier(UUID.randomUUID())
                .taskConfigKey("SITE_VISIT")
                .taskName("Site Visit")
                .dueAt(LocalDateTime.now().plusDays(1))
                .createdAt(LocalDateTime.now())
                .build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(taskReadService.findAllTasksForLead(leadIdentifier, true)).thenReturn(List.of(completedTask, openTask));
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        leadTaskTimelineService.refreshByLeadIdentifier(leadIdentifier);

        // Assert
        assertNotNull(lead.getTaskTimeline(), "Task timeline should be computed");
        assertNotNull(lead.getTaskTimeline().getNext(), "Next slot should be the open task");
        assertNotNull(lead.getTaskTimeline().getPrevious(), "Previous slot should be the completed task");
        assertEquals("SITE_VISIT", lead.getTaskTimeline().getNext().getTaskConfigKey(),
                "Next task should be the open site visit task");
        assertEquals("DOC_COLLECT", lead.getTaskTimeline().getPrevious().getTaskConfigKey(),
                "Previous task should be the completed document collection task");
    }
}
