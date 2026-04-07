package com.nivasafinance.features.lead.service.task;

import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.task.dto.TaskResponse;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Timeline rules: open tasks ordered by due_at (nulls last), then created_at.
 * Next: first open with due_at &gt;= now, or if none, earliest open (index 0).
 * Previous: open task at index next-1, or if next is first, most recently completed task.
 */
public final class LeadTaskTimelineComputer {

    private LeadTaskTimelineComputer() {
    }

    public static Lead.TaskTimeline compute(List<TaskResponse> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return null;
        }

        List<TaskResponse> open = tasks.stream()
                .filter(t -> !StringUtils.hasText(t.getOutcome()))
                .sorted(Comparator
                        .comparing(TaskResponse::getDueAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(TaskResponse::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        LocalDateTime now = LocalDateTime.now();
        int nextIdx = -1;
        for (int i = 0; i < open.size(); i++) {
            TaskResponse t = open.get(i);
            if (t.getDueAt() != null && !t.getDueAt().isBefore(now)) {
                nextIdx = i;
                break;
            }
        }
        if (nextIdx == -1 && !open.isEmpty()) {
            nextIdx = 0;
        }

        TaskResponse next = nextIdx >= 0 ? open.get(nextIdx) : null;
        TaskResponse previous = null;
        if (nextIdx > 0) {
            previous = open.get(nextIdx - 1);
        } else {
            previous = tasks.stream()
                    .filter(t -> StringUtils.hasText(t.getOutcome()))
                    .max(Comparator.comparing(LeadTaskTimelineComputer::completionTime,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .orElse(null);
        }

        if (previous == null && next == null) {
            return null;
        }

        return Lead.TaskTimeline.builder()
                .previous(toSlot(previous))
                .next(toSlot(next))
                .build();
    }

    private static LocalDateTime completionTime(TaskResponse t) {
        if (t.getOutcomeDetails() != null && t.getOutcomeDetails().getCompletedAt() != null) {
            return t.getOutcomeDetails().getCompletedAt();
        }
        return t.getUpdatedAt();
    }

    private static Lead.TaskTimelineSlot toSlot(TaskResponse t) {
        if (t == null) {
            return null;
        }
        return Lead.TaskTimelineSlot.builder()
                .taskIdentifier(t.getTaskIdentifier())
                .dueAt(t.getDueAt())
                .taskConfigKey(t.getTaskConfigKey())
                .taskName(t.getTaskName())
                .build();
    }
}
