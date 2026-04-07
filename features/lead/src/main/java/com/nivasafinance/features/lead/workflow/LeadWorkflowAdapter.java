package com.nivasafinance.features.lead.workflow;

import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.leadstages.dto.StageTransitionRequest;
import com.nivasafinance.features.leadstages.service.LeadStageHistoryWriteService;
import com.nivasafinance.features.task.dto.CreateTaskRequest;
import com.nivasafinance.features.task.dto.TaskDetailsRequest;
import com.nivasafinance.features.task.service.TaskWriteService;
import com.nivasafinance.features.workflow.adapter.EntityWorkflowAdapter;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

@Component
public class LeadWorkflowAdapter implements EntityWorkflowAdapter {
    
    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final TaskWriteService taskWriteService;
    private final LeadStageHistoryWriteService leadStageHistoryWriteService;

    public LeadWorkflowAdapter(
            LeadRepositoryWrapper leadRepositoryWrapper,
            TaskWriteService taskWriteService,
            @Lazy LeadStageHistoryWriteService leadStageHistoryWriteService) {
        this.leadRepositoryWrapper = leadRepositoryWrapper;
        this.taskWriteService = taskWriteService;
        this.leadStageHistoryWriteService = leadStageHistoryWriteService;
    }

    
    @Override
    public EntityType getEntityType() {
        return EntityType.LEAD;
    }
    
    @Override
    public Object getEntity(Long entityId) {
        return leadRepositoryWrapper.findByIdWithException(entityId);
    }
    
    @Override
    public UUID getEntityIdentifier(Object entity) {
        if (entity instanceof Lead) {
            return ((Lead) entity).getLeadIdentifier();
        }
        return null;
    }
    
    @Override
    public TaskDetailsRequest.PreferredCallWindow getPreferredCallWindow(Object entity) {
        if (entity instanceof Lead) {
            Lead lead = (Lead) entity;
            if (lead.getOtherDetails() != null) {
                LocalTime startTime = lead.getOtherDetails().getPreferredCallStartTime();
                LocalTime endTime = lead.getOtherDetails().getPreferredCallEndTime();
                
                if (startTime != null && endTime != null) {
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime startDateTime = LocalDateTime.of(now.toLocalDate(), startTime);
                    LocalDateTime endDateTime = LocalDateTime.of(now.toLocalDate(), endTime);
                    
                    if (endTime.isBefore(startTime)) {
                        endDateTime = endDateTime.plusDays(1);
                    }
                    
                    return TaskDetailsRequest.PreferredCallWindow.builder()
                            .start(startDateTime)
                            .end(endDateTime)
                            .build();
                }
            }
        }
        return null;
    }
    
    @Override
    public String getWorkflowConfigKey(Long entityId) {
        Lead lead = leadRepositoryWrapper.findByIdWithException(entityId);
        return lead.getWorkflowDetails() != null 
                ? lead.getWorkflowDetails().getWorkflowConfigKey() 
                : null;
    }
    
    @Override
    public Object createTaskAndAssociate(Long entityId, Object createTaskRequest, Map<String, Object> taskDetails) {
        if (createTaskRequest instanceof CreateTaskRequest) {
            return taskWriteService.createTask((CreateTaskRequest) createTaskRequest);
        }
        throw new IllegalArgumentException("Invalid CreateTaskRequest type");
    }

    @Override
    public Long resolveEntityId(UUID entityIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(entityIdentifier);
        return lead.getId();
    }

    @Override
    public void transitionStage(UUID entityIdentifier, String fromStageKey, String toStageKey, String assignTo) {
        StageTransitionRequest request = StageTransitionRequest.builder()
                .stageKey(toStageKey)
                .previousStageKey(fromStageKey)
                .assignedTo(assignTo)
                .build();
        leadStageHistoryWriteService.createStageEntry(entityIdentifier, request);
    }

    @Override
    public void closeOpenTasks(UUID entityIdentifier, String outcome) {
        taskWriteService.closeAllOpenTasksForLead(entityIdentifier, outcome);
    }

    @Override
    public void changeSubStage(UUID entityIdentifier, String stageKey, String subStageKey) {
        leadStageHistoryWriteService.changeSubStage(entityIdentifier, stageKey, subStageKey);
    }
}
