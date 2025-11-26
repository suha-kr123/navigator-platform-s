package com.nivasafinance.features.leadtasks.service.impl;

import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.leadstages.entity.LeadStageHistory;
import com.nivasafinance.features.leadstages.repository.LeadStageHistoryRepositoryWrapper;
import com.nivasafinance.features.leadtasks.dto.CreateAdhocTaskRequest;
import com.nivasafinance.features.leadtasks.dto.LeadCompleteTaskRequest;
import com.nivasafinance.features.leadtasks.dto.LeadReassignTaskRequest;
import com.nivasafinance.features.leadtasks.dto.LeadRescheduleTaskRequest;
import com.nivasafinance.features.leadtasks.dto.LeadTaskResponse;
import com.nivasafinance.features.leadtasks.entity.LeadTask;
import com.nivasafinance.features.leadtasks.repository.LeadTaskRepositoryWrapper;
import com.nivasafinance.features.leadtasks.service.LeadTaskWriteService;
import com.nivasafinance.features.task.dto.CompleteTaskRequest;
import com.nivasafinance.features.task.dto.CreateTaskRequest;
import com.nivasafinance.features.task.dto.ReassignTaskRequest;
import com.nivasafinance.features.task.dto.RescheduleTaskRequest;
import com.nivasafinance.features.task.dto.TaskDetailsRequest;
import com.nivasafinance.features.task.dto.TaskResponse;
import com.nivasafinance.features.task.entity.Task;
import com.nivasafinance.features.task.repository.TaskRepositoryWrapper;
import com.nivasafinance.features.task.service.TaskWriteService;
import com.nivasafinance.features.workflow.constants.WorkflowConstants;
import com.nivasafinance.features.workflow.orchestrator.WorkflowOrchestratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LeadTaskWriteServiceImpl implements LeadTaskWriteService {

    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final LeadStageHistoryRepositoryWrapper leadStageHistoryRepositoryWrapper;
    private final LeadTaskRepositoryWrapper leadTaskRepositoryWrapper;
    private final TaskWriteService taskWriteService;
    private final TaskRepositoryWrapper taskRepositoryWrapper;
    private final WorkflowOrchestratorService workflowOrchestratorService;

    @Override
    public LeadTaskResponse createAdhocTask(UUID leadIdentifier, CreateAdhocTaskRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        
        // Get current stage from lead workflow details or use provided stageKey
        String stageKey = getStageKey(lead, request.getStageKey());
        
        Object result = workflowOrchestratorService.createAdhocTaskForStage(
                lead.getId(), 
                EntityType.LEAD, 
                stageKey, 
                request.getTaskConfigKey()
        );
        
        return (LeadTaskResponse) result;
    }

    @Override
    public LeadTaskResponse createTaskAndAssociateWithLead(Long leadId, CreateTaskRequest request, Map<String, Object> taskDetails) {
        // Create the task
        TaskResponse taskResponse = taskWriteService.createTask(request);
        
        // Extract stageKey from taskDetails
        String stageKey = (String) taskDetails.get(WorkflowConstants.TaskDetails.STAGE_KEY);
        
        // Create LeadTask association
        LeadTask.TaskDetails leadTaskDetails = LeadTask.TaskDetails.builder()
                .stageKey(stageKey)
                .build();
        
        LeadTask leadTask = LeadTask.builder()
                .leadId(leadId)
                .taskId(taskResponse.getId())
                .taskDetails(leadTaskDetails)
                .build();
        
        LeadTask savedLeadTask = leadTaskRepositoryWrapper.save(leadTask);
        
        return LeadTaskResponse.from(savedLeadTask, leadRepositoryWrapper.findByIdWithException(leadId).getLeadIdentifier(), taskResponse);
    }

    private String getStageKey(Lead lead, String providedStageKey) {
        if (ValidationUtils.isNonNull(providedStageKey)) {
            return providedStageKey;
        }
        
        Lead.WorkflowDetails workflowDetails = lead.getWorkflowDetails();
        if (ValidationUtils.isNonNull(workflowDetails) 
                && ValidationUtils.isNonNull(workflowDetails.getCurrentStageDetails())) {
            String stageKey = workflowDetails.getCurrentStageDetails().getStageKey();
            if (ValidationUtils.isNonNull(stageKey)) {
                return stageKey;
            }
        }
        
        // Fallback to latest stage history
        Optional<LeadStageHistory> latestEntry = leadStageHistoryRepositoryWrapper.findLatestEntry(lead.getId());
        if (latestEntry.isPresent()) {
            return latestEntry.get().getStageKey();
        }
        
        throw new IllegalStateException("Cannot determine stage for lead: " + lead.getLeadIdentifier());
    }

    @Override
    public LeadTaskResponse completeTask(UUID leadIdentifier, LeadCompleteTaskRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        
        // Convert LeadCompleteTaskRequest to CompleteTaskRequest
        CompleteTaskRequest completeTaskRequest = CompleteTaskRequest.builder()
                .taskIdentifier(request.getTaskIdentifier())
                .outcomeCodeValueKey(request.getOutcomeCodeValueKey())
                .outcomeDetails(request.getRemarks() != null 
                        ? CompleteTaskRequest.OutcomeDetailsRequest.builder()
                                .remarks(request.getRemarks())
                                .build()
                        : null)
                .build();
        
        // Complete the task directly (avoiding circular dependency with orchestrator)
        TaskResponse taskResponse = taskWriteService.completeTask(completeTaskRequest);
        
        // Find the associated LeadTask
        LeadTask leadTask = leadTaskRepositoryWrapper.findByTaskId(taskResponse.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "LeadTask not found for taskId: " + taskResponse.getId() + " and leadId: " + lead.getId()));
        
        // Return LeadTaskResponse
        return LeadTaskResponse.from(leadTask, leadIdentifier, taskResponse);
    }

    @Override
    public LeadTaskResponse reassignTask(UUID leadIdentifier, LeadReassignTaskRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        
        // Convert LeadReassignTaskRequest to ReassignTaskRequest
        ReassignTaskRequest reassignTaskRequest = ReassignTaskRequest.builder()
                .taskIdentifier(request.getTaskIdentifier())
                .newAssignedTo(request.getNewAssignedTo())
                .build();
        
        // Reassign the task directly (avoiding circular dependency with orchestrator)
        TaskResponse taskResponse = taskWriteService.reassignTask(reassignTaskRequest);
        
        // Find the associated LeadTask
        LeadTask leadTask = leadTaskRepositoryWrapper.findByTaskId(taskResponse.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "LeadTask not found for taskId: " + taskResponse.getId() + " and leadId: " + lead.getId()));
        
        // Return LeadTaskResponse
        return LeadTaskResponse.from(leadTask, leadIdentifier, taskResponse);
    }

    @Override
    public LeadTaskResponse rescheduleTask(UUID leadIdentifier, LeadRescheduleTaskRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        
        // Get the old task before rescheduling (to extract details for new task creation)
        Task oldTask = taskRepositoryWrapper.findByTaskIdentifierWithException(request.getTaskIdentifier());
        
        // Convert LeadRescheduleTaskRequest to RescheduleTaskRequest
        RescheduleTaskRequest rescheduleTaskRequest = RescheduleTaskRequest.builder()
                .taskIdentifier(request.getTaskIdentifier())
                .preferredStartTime(request.getPreferredStartTime())
                .preferredEndTime(request.getPreferredEndTime())
                .reasonCodeValueKey(request.getReasonCodeValueKey())
                .creatorRemarks(request.getCreatorRemarks())
                .build();
        
        // Reschedule the task (closes old task)
        TaskResponse taskResponse = taskWriteService.rescheduleTask(rescheduleTaskRequest);
        
        // Create new task synchronously with same config, new preferred times, iteration+1
        if (ValidationUtils.isNonNull(oldTask.getTaskDetails()) 
                && ValidationUtils.isNonNull(oldTask.getTaskDetails().getEntityType())
                && ValidationUtils.isNonNull(oldTask.getTaskDetails().getEntityId())
                && oldTask.getTaskDetails().getEntityType() == EntityType.LEAD) {
            
            // Build preferred call window from request or use old task's window
            TaskDetailsRequest.PreferredCallWindow preferredCallWindow = null;
            if (ValidationUtils.isNonNull(request.getPreferredStartTime()) 
                    && ValidationUtils.isNonNull(request.getPreferredEndTime())) {
                preferredCallWindow = TaskDetailsRequest.PreferredCallWindow.builder()
                        .start(request.getPreferredStartTime())
                        .end(request.getPreferredEndTime())
                        .build();
            } else if (ValidationUtils.isNonNull(oldTask.getTaskDetails().getPreferredCallWindow())) {
                preferredCallWindow = TaskDetailsRequest.PreferredCallWindow.builder()
                        .start(oldTask.getTaskDetails().getPreferredCallWindow().getStart())
                        .end(oldTask.getTaskDetails().getPreferredCallWindow().getEnd())
                        .build();
            }
            
            // Use user-provided creator remarks, or build default if not provided
            String creatorRemarks = request.getCreatorRemarks();
            if (!ValidationUtils.isNonNull(creatorRemarks)) {
                // Fallback: build default remarks if user didn't provide any
                creatorRemarks = "Rescheduled from task " + oldTask.getTaskIdentifier();
                if (ValidationUtils.isNonNull(oldTask.getTaskDetails().getCreatorRemarks())) {
                    creatorRemarks += ". Previous: " + oldTask.getTaskDetails().getCreatorRemarks();
                }
                if (ValidationUtils.isNonNull(request.getReasonCodeValueKey())) {
                    creatorRemarks += ". Reason: " + request.getReasonCodeValueKey();
                }
            }
            
            // Increment iteration count
            Integer oldIterationCount = oldTask.getTaskDetails().getIterationCount();
            Integer newIterationCount = (ValidationUtils.isNonNull(oldIterationCount) ? oldIterationCount : 0) + 1;
            
            // Get stage key from old LeadTask
            LeadTask oldLeadTask = leadTaskRepositoryWrapper.findByTaskId(oldTask.getId())
                    .orElseThrow(() -> new IllegalStateException("LeadTask not found for old taskId: " + oldTask.getId()));
            String stageKey = oldLeadTask.getTaskDetails() != null 
                    ? oldLeadTask.getTaskDetails().getStageKey() 
                    : null;
            
            // Create new task
            TaskDetailsRequest taskDetails = TaskDetailsRequest.builder()
                    .entityId(oldTask.getTaskDetails().getEntityId())
                    .entityType(oldTask.getTaskDetails().getEntityType())
                    .preferredCallWindow(preferredCallWindow)
                    .creatorRemarks(creatorRemarks)
                    .iterationCount(newIterationCount)
                    .build();
            
            CreateTaskRequest createTaskRequest = CreateTaskRequest.builder()
                    .taskConfigKey(oldTask.getTaskConfigKey())
                    .assignedTo(oldTask.getAssignedTo())
                    .dueAt(request.getPreferredEndTime() != null ? request.getPreferredEndTime() : oldTask.getDueAt())
                    .taskDetails(taskDetails)
                    .build();
            
            // Create and associate new task with lead
            if (ValidationUtils.isNonNull(stageKey)) {
                Map<String, Object> newTaskDetails = new java.util.HashMap<>();
                newTaskDetails.put(WorkflowConstants.TaskDetails.STAGE_KEY, stageKey);
                createTaskAndAssociateWithLead(lead.getId(), createTaskRequest, newTaskDetails);
            } else {
                // If no stage key, just create the task
                taskWriteService.createTask(createTaskRequest);
            }
        }
        
        // Find the associated LeadTask for the old (now closed) task
        LeadTask leadTask = leadTaskRepositoryWrapper.findByTaskId(taskResponse.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "LeadTask not found for taskId: " + taskResponse.getId() + " and leadId: " + lead.getId()));
        
        // Return LeadTaskResponse
        return LeadTaskResponse.from(leadTask, leadIdentifier, taskResponse);
    }
}