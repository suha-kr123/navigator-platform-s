package com.nivasafinance.features.leadtasks.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.task.dto.CreateTaskRequest;
import com.nivasafinance.features.task.dto.RescheduleTaskRequest;
import com.nivasafinance.features.task.dto.TaskDetailsRequest;
import com.nivasafinance.features.task.entity.Task;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadRescheduleTaskRequest {
    
    @NotNull(message = "Task ID is required")
    private UUID taskIdentifier;

    @NotNull(message = "Preferred start time is required")
    private LocalDateTime preferredStartTime;
    
    @NotNull(message = "Preferred end time is required")
    private LocalDateTime preferredEndTime;
    
    private String reasonCodeValueKey;
    
    private String creatorRemarks;
    
    private UUID rescheduledFromTaskIdentifier;

    private Map<String, Object> locationDetails;

    public static RescheduleTaskRequest toRescheduleTaskRequest(LeadRescheduleTaskRequest request) {
        return RescheduleTaskRequest.builder()
                .taskIdentifier(request.getTaskIdentifier())
                .preferredStartTime(request.getPreferredStartTime())
                .preferredEndTime(request.getPreferredEndTime())
                .reasonCodeValueKey(request.getReasonCodeValueKey())
                .creatorRemarks(request.getCreatorRemarks())
                .rescheduledFromTaskIdentifier(request.getRescheduledFromTaskIdentifier())
                .locationDetails(ValidationUtils.isNonNull(request.getLocationDetails()) 
                        ? request.getLocationDetails()
                        : null)
                .build();
    }

    public static CreateTaskRequest buildCreateTaskRequest(Task oldTask, LeadRescheduleTaskRequest request,
            UUID rescheduledFromTaskIdentifier, String rescheduledFromTaskRemarks,
            TaskDetailsRequest.PreferredCallWindow preferredCallWindow) {
        Task.TaskDetails oldTaskDetails = oldTask.getTaskDetails();
        int oldIterationCount = 0;
        if (ValidationUtils.isNonNull(oldTaskDetails) && ValidationUtils.isNonNull(oldTaskDetails.getIterationCount())) {
            oldIterationCount = oldTaskDetails.getIterationCount();
        }
        int newIterationCount = oldIterationCount + 1;

        return CreateTaskRequest.builder()
                .taskConfigKey(oldTask.getTaskConfigKey())
                .assignedTo(oldTask.getAssignedTo())
                .dueAt(request.getPreferredEndTime() != null ? request.getPreferredEndTime() : oldTask.getDueAt())
                .taskDetails(TaskDetailsRequest.builder()
                        .entityId(ValidationUtils.isNonNull(oldTaskDetails) ? oldTaskDetails.getEntityId() : null)
                        .entityType(ValidationUtils.isNonNull(oldTaskDetails) ? oldTaskDetails.getEntityType() : null)
                        .preferredCallWindow(preferredCallWindow)
                        .creatorRemarks(request.getCreatorRemarks())
                        .iterationCount(newIterationCount)
                        .rescheduledFromTaskIdentifier(rescheduledFromTaskIdentifier)
                        .rescheduleReasonCodeValueKey(request.getReasonCodeValueKey())
                        .rescheduledFromTaskRemarks(rescheduledFromTaskRemarks)
                        .build())
                .build();
    }

    public static TaskDetailsRequest.PreferredCallWindow buildPreferredCallWindow(Task oldTask, LeadRescheduleTaskRequest request) {
        if (ValidationUtils.isNonNull(request.getPreferredStartTime()) && ValidationUtils.isNonNull(request.getPreferredEndTime())) {
            return TaskDetailsRequest.PreferredCallWindow.builder()
                    .start(request.getPreferredStartTime())
                    .end(request.getPreferredEndTime())
                    .build();
        }
        if (ValidationUtils.isNonNull(oldTask.getTaskDetails()) && ValidationUtils.isNonNull(oldTask.getTaskDetails().getPreferredCallWindow())) {
            return TaskDetailsRequest.PreferredCallWindow.builder()
                    .start(oldTask.getTaskDetails().getPreferredCallWindow().getStart())
                    .end(oldTask.getTaskDetails().getPreferredCallWindow().getEnd())
                    .build();
        }
        return null;
    }
}

