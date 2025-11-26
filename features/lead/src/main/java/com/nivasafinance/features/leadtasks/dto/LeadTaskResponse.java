package com.nivasafinance.features.leadtasks.dto;

import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.leadtasks.entity.LeadTask;
import com.nivasafinance.features.task.dto.TaskResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadTaskResponse {
    
    private Long id;
    private UUID leadIdentifier;
    private UUID taskIdentifier;
    private String taskConfigKey;
    private String taskName;
    private String taskDescription;
    private String assignedTo;
    private LocalDateTime dueAt;
    
    private String outcome;
    private OutcomeDetails outcomeDetails;
    
    private TaskDetails taskDetails;
    private String stageKey;
    
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OutcomeDetails {
        private String remarks;
        private LocalDateTime completedAt;
        private String completedBy;
        private String rescheduleReasonCodeValueKey;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaskDetails {
        private PreferredCallWindow preferredCallWindow;
        private String creatorRemarks;
        private Integer iterationCount;
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class PreferredCallWindow {
            private LocalDateTime start;
            private LocalDateTime end;
        }
    }
    
    public static LeadTaskResponse from(LeadTask leadTask, UUID leadIdentifier, TaskResponse taskResponse) {
        OutcomeDetails outcomeDetails = null;
        if (ValidationUtils.isNonNull(taskResponse.getOutcomeDetails())) {
            outcomeDetails = OutcomeDetails.builder()
                    .remarks(taskResponse.getOutcomeDetails().getRemarks())
                    .completedAt(taskResponse.getOutcomeDetails().getCompletedAt())
                    .completedBy(taskResponse.getOutcomeDetails().getCompletedBy())
                    .rescheduleReasonCodeValueKey(taskResponse.getOutcomeDetails().getRescheduleReasonCodeValueKey())
                    .build();
        }
        
        TaskDetails taskDetails = null;
        if (ValidationUtils.isNonNull(taskResponse.getTaskDetails())) {
            TaskDetails.PreferredCallWindow preferredCallWindow = null;
            if (ValidationUtils.isNonNull(taskResponse.getTaskDetails().getPreferredCallWindow())) {
                preferredCallWindow = TaskDetails.PreferredCallWindow.builder()
                        .start(taskResponse.getTaskDetails().getPreferredCallWindow().getStart())
                        .end(taskResponse.getTaskDetails().getPreferredCallWindow().getEnd())
                        .build();
            }
            taskDetails = TaskDetails.builder()
                    .preferredCallWindow(preferredCallWindow)
                    .creatorRemarks(taskResponse.getTaskDetails().getCreatorRemarks())
                    .iterationCount(taskResponse.getTaskDetails().getIterationCount())
                    .build();
        }
        
        return LeadTaskResponse.builder()
                .id(leadTask.getId())
                .leadIdentifier(leadIdentifier)
                .taskIdentifier(taskResponse.getTaskIdentifier())
                .taskConfigKey(taskResponse.getTaskConfigKey())
                .taskName(taskResponse.getTaskName())
                .taskDescription(taskResponse.getTaskDescription())
                .assignedTo(taskResponse.getAssignedTo())
                .dueAt(taskResponse.getDueAt())
                .outcome(taskResponse.getOutcome())
                .outcomeDetails(outcomeDetails)
                .taskDetails(taskDetails)
                .stageKey(extractStageKey(leadTask))
                .createdAt(leadTask.getCreatedAt())
                .createdBy(leadTask.getCreatedBy())
                .updatedAt(leadTask.getUpdatedAt())
                .updatedBy(leadTask.getUpdatedBy())
                .build();
    }
    
    public static LeadTaskResponse from(LeadTask leadTask, UUID leadIdentifier, TaskResponse taskResponse, boolean allowNullLeadTask) {
        if (allowNullLeadTask && !ValidationUtils.isNonNull(leadTask)) {
            OutcomeDetails outcomeDetails = null;
            if (ValidationUtils.isNonNull(taskResponse.getOutcomeDetails())) {
                outcomeDetails = OutcomeDetails.builder()
                        .remarks(taskResponse.getOutcomeDetails().getRemarks())
                        .completedAt(taskResponse.getOutcomeDetails().getCompletedAt())
                        .completedBy(taskResponse.getOutcomeDetails().getCompletedBy())
                        .rescheduleReasonCodeValueKey(taskResponse.getOutcomeDetails().getRescheduleReasonCodeValueKey())
                        .build();
            }
            
            TaskDetails taskDetails = null;
            if (ValidationUtils.isNonNull(taskResponse.getTaskDetails())) {
                TaskDetails.PreferredCallWindow preferredCallWindow = null;
                if (ValidationUtils.isNonNull(taskResponse.getTaskDetails().getPreferredCallWindow())) {
                    preferredCallWindow = TaskDetails.PreferredCallWindow.builder()
                            .start(taskResponse.getTaskDetails().getPreferredCallWindow().getStart())
                            .end(taskResponse.getTaskDetails().getPreferredCallWindow().getEnd())
                            .build();
                }
                taskDetails = TaskDetails.builder()
                        .preferredCallWindow(preferredCallWindow)
                        .creatorRemarks(taskResponse.getTaskDetails().getCreatorRemarks())
                        .iterationCount(taskResponse.getTaskDetails().getIterationCount())
                        .build();
            }
            
            return LeadTaskResponse.builder()
                    .id(null)
                    .leadIdentifier(leadIdentifier)
                    .taskIdentifier(taskResponse.getTaskIdentifier())
                    .taskConfigKey(taskResponse.getTaskConfigKey())
                    .taskName(taskResponse.getTaskName())
                    .taskDescription(taskResponse.getTaskDescription())
                    .assignedTo(taskResponse.getAssignedTo())
                    .dueAt(taskResponse.getDueAt())
                    .outcome(taskResponse.getOutcome())
                    .outcomeDetails(outcomeDetails)
                    .taskDetails(taskDetails)
                    .stageKey(null)
                    .createdAt(null)
                    .createdBy(null)
                    .updatedAt(null)
                    .updatedBy(null)
                    .build();
        }
        return from(leadTask, leadIdentifier, taskResponse);
    }

    private static String extractStageKey(LeadTask leadTask) {
        return ValidationUtils.isNonNull(leadTask) && ValidationUtils.isNonNull(leadTask.getTaskDetails()) 
                ? leadTask.getTaskDetails().getStageKey() : null;
    }
}

