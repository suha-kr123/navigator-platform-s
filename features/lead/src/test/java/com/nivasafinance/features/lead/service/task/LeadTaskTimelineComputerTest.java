package com.nivasafinance.features.lead.service.task;

import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.task.dto.OutcomeDetailsResponse;
import com.nivasafinance.features.task.dto.TaskResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LeadTaskTimelineComputerTest {

    // ==================== null / empty input ====================

    @Test
    void compute_withNullTasks_returnsNull() {
        assertNull(LeadTaskTimelineComputer.compute(null));
    }

    @Test
    void compute_withEmptyTasks_returnsNull() {
        assertNull(LeadTaskTimelineComputer.compute(Collections.emptyList()));
    }

    // ==================== single open task ====================

    @Test
    void compute_withSingleOpenTaskFutureDue_returnsNextOnly() {
        // Arrange
        UUID taskId = UUID.randomUUID();
        TaskResponse open = TaskResponse.builder()
                .taskIdentifier(taskId).taskConfigKey("CALL").taskName("Follow-up Call")
                .dueAt(LocalDateTime.now().plusDays(1))
                .createdAt(LocalDateTime.now())
                .build();

        // Act
        Lead.TaskTimeline result = LeadTaskTimelineComputer.compute(List.of(open));

        // Assert
        assertNotNull(result);
        assertNotNull(result.getNext(), "Next should be the open task");
        assertEquals(taskId, result.getNext().getTaskIdentifier());
        assertNull(result.getPrevious(), "Previous should be null with no completed tasks");
    }

    @Test
    void compute_withSingleOpenTaskPastDue_fallsBackToFirstOpen() {
        // Arrange
        UUID taskId = UUID.randomUUID();
        TaskResponse open = TaskResponse.builder()
                .taskIdentifier(taskId).taskConfigKey("CALL").taskName("Overdue Call")
                .dueAt(LocalDateTime.now().minusDays(1))
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();

        // Act
        Lead.TaskTimeline result = LeadTaskTimelineComputer.compute(List.of(open));

        // Assert
        assertNotNull(result);
        assertNotNull(result.getNext());
        assertEquals(taskId, result.getNext().getTaskIdentifier(),
                "Should fall back to first open when all due dates are in the past");
    }

    @Test
    void compute_withSingleOpenTaskNullDue_fallsBackToFirstOpen() {
        // Arrange
        UUID taskId = UUID.randomUUID();
        TaskResponse open = TaskResponse.builder()
                .taskIdentifier(taskId).taskConfigKey("CALL").taskName("No Due")
                .dueAt(null)
                .createdAt(LocalDateTime.now())
                .build();

        // Act
        Lead.TaskTimeline result = LeadTaskTimelineComputer.compute(List.of(open));

        // Assert
        assertNotNull(result);
        assertEquals(taskId, result.getNext().getTaskIdentifier(),
                "Task with null dueAt should still be selected via fallback");
    }

    // ==================== multiple open tasks – next selection ====================

    @Test
    void compute_withMultipleOpenTasks_selectsFirstFutureDueAsNext() {
        // Arrange
        UUID pastId = UUID.randomUUID();
        UUID futureId = UUID.randomUUID();

        TaskResponse pastDue = TaskResponse.builder()
                .taskIdentifier(pastId).taskConfigKey("CALL").taskName("Past")
                .dueAt(LocalDateTime.now().minusDays(2))
                .createdAt(LocalDateTime.now().minusDays(3))
                .build();

        TaskResponse futureDue = TaskResponse.builder()
                .taskIdentifier(futureId).taskConfigKey("VISIT").taskName("Future")
                .dueAt(LocalDateTime.now().plusDays(1))
                .createdAt(LocalDateTime.now())
                .build();

        // Act
        Lead.TaskTimeline result = LeadTaskTimelineComputer.compute(List.of(pastDue, futureDue));

        // Assert
        assertNotNull(result);
        assertEquals(futureId, result.getNext().getTaskIdentifier(),
                "Next should be the first open task with dueAt >= now");
    }

    @Test
    void compute_withMultipleOpenTasks_previousFromOpenList() {
        // Arrange — two past-due + one future, sorted by dueAt
        UUID task1Id = UUID.randomUUID();
        UUID task2Id = UUID.randomUUID();
        UUID task3Id = UUID.randomUUID();

        TaskResponse t1 = TaskResponse.builder()
                .taskIdentifier(task1Id).taskConfigKey("A").taskName("A")
                .dueAt(LocalDateTime.now().minusDays(3))
                .createdAt(LocalDateTime.now().minusDays(4))
                .build();

        TaskResponse t2 = TaskResponse.builder()
                .taskIdentifier(task2Id).taskConfigKey("B").taskName("B")
                .dueAt(LocalDateTime.now().minusDays(1))
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();

        TaskResponse t3 = TaskResponse.builder()
                .taskIdentifier(task3Id).taskConfigKey("C").taskName("C")
                .dueAt(LocalDateTime.now().plusDays(2))
                .createdAt(LocalDateTime.now())
                .build();

        // Act
        Lead.TaskTimeline result = LeadTaskTimelineComputer.compute(List.of(t1, t2, t3));

        // Assert
        assertNotNull(result);
        assertEquals(task3Id, result.getNext().getTaskIdentifier(),
                "Next should be the first future-due open task");
        assertEquals(task2Id, result.getPrevious().getTaskIdentifier(),
                "Previous should be the open task at index next-1");
    }

    // ==================== previous from completed tasks ====================

    @Test
    void compute_withOpenAndCompletedTasks_previousFromCompleted() {
        // Arrange
        UUID openId = UUID.randomUUID();
        UUID completedId = UUID.randomUUID();

        TaskResponse openTask = TaskResponse.builder()
                .taskIdentifier(openId).taskConfigKey("CALL").taskName("Open")
                .dueAt(LocalDateTime.now().plusDays(1))
                .createdAt(LocalDateTime.now())
                .build();

        TaskResponse completedTask = TaskResponse.builder()
                .taskIdentifier(completedId).taskConfigKey("VISIT").taskName("Done")
                .dueAt(LocalDateTime.now().minusDays(2))
                .createdAt(LocalDateTime.now().minusDays(3))
                .outcome("COMPLETED")
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();

        // Act
        Lead.TaskTimeline result = LeadTaskTimelineComputer.compute(List.of(openTask, completedTask));

        // Assert
        assertNotNull(result);
        assertEquals(openId, result.getNext().getTaskIdentifier());
        assertEquals(completedId, result.getPrevious().getTaskIdentifier(),
                "Previous should come from most recently completed task when next is first open");
    }

    // ==================== completionTime logic ====================

    @Test
    void compute_completionTimeUsesOutcomeDetailsCompletedAt() {
        // Arrange
        UUID openId = UUID.randomUUID();
        UUID completed1Id = UUID.randomUUID();
        UUID completed2Id = UUID.randomUUID();

        TaskResponse openTask = TaskResponse.builder()
                .taskIdentifier(openId).taskConfigKey("CALL").taskName("Open")
                .dueAt(LocalDateTime.now().plusDays(1))
                .createdAt(LocalDateTime.now())
                .build();

        TaskResponse completed1 = TaskResponse.builder()
                .taskIdentifier(completed1Id).taskConfigKey("A").taskName("Older")
                .outcome("DONE")
                .updatedAt(LocalDateTime.now().minusDays(5))
                .outcomeDetails(OutcomeDetailsResponse.builder()
                        .completedAt(LocalDateTime.now().minusDays(3)).build())
                .createdAt(LocalDateTime.now().minusDays(6))
                .build();

        TaskResponse completed2 = TaskResponse.builder()
                .taskIdentifier(completed2Id).taskConfigKey("B").taskName("Newer")
                .outcome("DONE")
                .updatedAt(LocalDateTime.now().minusDays(10))
                .outcomeDetails(OutcomeDetailsResponse.builder()
                        .completedAt(LocalDateTime.now().minusDays(1)).build())
                .createdAt(LocalDateTime.now().minusDays(11))
                .build();

        // Act
        Lead.TaskTimeline result = LeadTaskTimelineComputer.compute(
                List.of(openTask, completed1, completed2));

        // Assert
        assertNotNull(result);
        assertEquals(completed2Id, result.getPrevious().getTaskIdentifier(),
                "Should pick completed task with most recent completedAt from outcomeDetails");
    }

    @Test
    void compute_completionTimeFallsBackToUpdatedAt() {
        // Arrange
        UUID openId = UUID.randomUUID();
        UUID completedId = UUID.randomUUID();

        TaskResponse openTask = TaskResponse.builder()
                .taskIdentifier(openId).taskConfigKey("CALL").taskName("Open")
                .dueAt(LocalDateTime.now().plusDays(1))
                .createdAt(LocalDateTime.now())
                .build();

        TaskResponse completed = TaskResponse.builder()
                .taskIdentifier(completedId).taskConfigKey("A").taskName("Done")
                .outcome("DONE")
                .outcomeDetails(null)
                .updatedAt(LocalDateTime.now().minusDays(1))
                .createdAt(LocalDateTime.now().minusDays(3))
                .build();

        // Act
        Lead.TaskTimeline result = LeadTaskTimelineComputer.compute(List.of(openTask, completed));

        // Assert
        assertNotNull(result);
        assertEquals(completedId, result.getPrevious().getTaskIdentifier(),
                "Should use updatedAt when outcomeDetails is null");
    }

    @Test
    void compute_completionTimeFallsBackToUpdatedAtWhenCompletedAtNull() {
        // Arrange
        UUID openId = UUID.randomUUID();
        UUID completedId = UUID.randomUUID();

        TaskResponse openTask = TaskResponse.builder()
                .taskIdentifier(openId).taskConfigKey("CALL").taskName("Open")
                .dueAt(LocalDateTime.now().plusDays(1))
                .createdAt(LocalDateTime.now())
                .build();

        TaskResponse completed = TaskResponse.builder()
                .taskIdentifier(completedId).taskConfigKey("A").taskName("Done")
                .outcome("DONE")
                .outcomeDetails(OutcomeDetailsResponse.builder().completedAt(null).build())
                .updatedAt(LocalDateTime.now().minusDays(1))
                .createdAt(LocalDateTime.now().minusDays(3))
                .build();

        // Act
        Lead.TaskTimeline result = LeadTaskTimelineComputer.compute(List.of(openTask, completed));

        // Assert
        assertNotNull(result);
        assertEquals(completedId, result.getPrevious().getTaskIdentifier(),
                "Should use updatedAt when outcomeDetails.completedAt is null");
    }

    // ==================== only completed tasks ====================

    @Test
    void compute_withOnlyCompletedTasks_returnsPreviousOnlyTimeline() {
        // Arrange
        UUID completedId = UUID.randomUUID();
        TaskResponse completed = TaskResponse.builder()
                .taskIdentifier(completedId).taskConfigKey("CALL").taskName("Done")
                .outcome("COMPLETED")
                .updatedAt(LocalDateTime.now().minusDays(1))
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();

        // Act
        Lead.TaskTimeline result = LeadTaskTimelineComputer.compute(List.of(completed));

        // Assert
        assertNotNull(result, "Should return timeline when there is a completed task");
        assertNull(result.getNext(), "Next should be null when no open tasks exist");
        assertEquals(completedId, result.getPrevious().getTaskIdentifier(),
                "Previous should be the most recently completed task");
    }

    // ==================== sorting by dueAt then createdAt ====================

    @Test
    void compute_openTasksSortedByDueAtThenCreatedAt() {
        // Arrange — two tasks with same dueAt, different createdAt
        UUID earlierCreated = UUID.randomUUID();
        UUID laterCreated = UUID.randomUUID();

        LocalDateTime sameDue = LocalDateTime.now().plusDays(1);

        TaskResponse t1 = TaskResponse.builder()
                .taskIdentifier(laterCreated).taskConfigKey("A").taskName("Later")
                .dueAt(sameDue)
                .createdAt(LocalDateTime.now())
                .build();

        TaskResponse t2 = TaskResponse.builder()
                .taskIdentifier(earlierCreated).taskConfigKey("B").taskName("Earlier")
                .dueAt(sameDue)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        // Act
        Lead.TaskTimeline result = LeadTaskTimelineComputer.compute(List.of(t1, t2));

        // Assert
        assertNotNull(result);
        assertEquals(earlierCreated, result.getNext().getTaskIdentifier(),
                "When dueAt is equal, earlier createdAt should come first");
    }

    // ==================== toSlot mapping ====================

    @Test
    void compute_slotMapsAllFields() {
        // Arrange
        UUID taskId = UUID.randomUUID();
        LocalDateTime dueAt = LocalDateTime.now().plusDays(1);
        TaskResponse open = TaskResponse.builder()
                .taskIdentifier(taskId).taskConfigKey("FIELD_VISIT")
                .taskName("Site Inspection").dueAt(dueAt)
                .createdAt(LocalDateTime.now())
                .build();

        // Act
        Lead.TaskTimeline result = LeadTaskTimelineComputer.compute(List.of(open));

        // Assert
        assertNotNull(result.getNext());
        assertEquals(taskId, result.getNext().getTaskIdentifier());
        assertEquals(dueAt, result.getNext().getDueAt());
        assertEquals("FIELD_VISIT", result.getNext().getTaskConfigKey());
        assertEquals("Site Inspection", result.getNext().getTaskName());
    }
}
