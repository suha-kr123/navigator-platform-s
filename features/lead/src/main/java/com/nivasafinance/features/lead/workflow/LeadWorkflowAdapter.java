package com.nivasafinance.features.lead.workflow;

import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.leadtasks.dto.CreateAdhocTaskRequest;
import com.nivasafinance.features.leadtasks.service.LeadTaskWriteService;
import com.nivasafinance.features.task.dto.CreateTaskRequest;
import com.nivasafinance.features.task.dto.TaskDetailsRequest;
import com.nivasafinance.features.workflow.adapter.EntityWorkflowAdapter;
import com.nivasafinance.features.workflow.constants.WorkflowConstants;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class LeadWorkflowAdapter implements EntityWorkflowAdapter {
    
    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final LeadTaskWriteService leadTaskWriteService;
    
    public LeadWorkflowAdapter(
            LeadRepositoryWrapper leadRepositoryWrapper,
            @Lazy LeadTaskWriteService leadTaskWriteService) {
        this.leadRepositoryWrapper = leadRepositoryWrapper;
        this.leadTaskWriteService = leadTaskWriteService;
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
    public Object createAdhocTaskRequest(String taskConfigKey, String stageKey) {
        return CreateAdhocTaskRequest.builder()
                .taskConfigKey(taskConfigKey)
                .stageKey(stageKey)
                .build();
    }
    
    @Override
    public Object createTaskAndAssociate(Long entityId, Object createTaskRequest, java.util.Map<String, Object> taskDetails) {
        if (createTaskRequest instanceof CreateTaskRequest) {
            return leadTaskWriteService.createTaskAndAssociateWithLead(entityId, (CreateTaskRequest) createTaskRequest, taskDetails);
        }
        throw new IllegalArgumentException("Invalid CreateTaskRequest type");
    }
    
    @Override
    public Object createAdhocTask(UUID entityIdentifier, Object createAdhocTaskRequest) {
        if (createAdhocTaskRequest instanceof CreateAdhocTaskRequest) {
            CreateAdhocTaskRequest adhocRequest = (CreateAdhocTaskRequest) createAdhocTaskRequest;
            
            // Get the lead to get entityId
            Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(entityIdentifier);
            
            // Convert CreateAdhocTaskRequest to CreateTaskRequest
            TaskDetailsRequest.PreferredCallWindow preferredCallWindow = null;
            if (ValidationUtils.isNonNull(adhocRequest.getPreferredCallWindowStart()) 
                    && ValidationUtils.isNonNull(adhocRequest.getPreferredCallWindowEnd())) {
                preferredCallWindow = TaskDetailsRequest.PreferredCallWindow.builder()
                        .start(adhocRequest.getPreferredCallWindowStart())
                        .end(adhocRequest.getPreferredCallWindowEnd())
                        .build();
            }
            
            TaskDetailsRequest taskDetails = TaskDetailsRequest.builder()
                    .entityId(lead.getLeadIdentifier())
                    .entityType(EntityType.LEAD)
                    .creatorRemarks(adhocRequest.getCreatorRemarks())
                    .preferredCallWindow(preferredCallWindow)
                    .build();
            
            CreateTaskRequest createTaskRequest = CreateTaskRequest.builder()
                    .taskConfigKey(adhocRequest.getTaskConfigKey())
                    .assignedTo(adhocRequest.getAssignedTo())
                    .dueAt(adhocRequest.getDueAt())
                    .taskDetails(taskDetails)
                    .build();
            
            // Build task details map with stageKey
            Map<String, Object> taskDetailsMap = new HashMap<>();
            taskDetailsMap.put(WorkflowConstants.TaskDetails.STAGE_KEY, adhocRequest.getStageKey());
            
            // Use createTaskAndAssociate to avoid circular dependency
            return leadTaskWriteService.createTaskAndAssociateWithLead(lead.getId(), createTaskRequest, taskDetailsMap);
        }
        throw new IllegalArgumentException("Invalid CreateAdhocTaskRequest type");
    }
}

