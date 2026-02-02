package com.nivasafinance.features.leadtasks.service.impl;

import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.workflow.LeadWorkflowAdapter;
import com.nivasafinance.features.leadtasks.dto.CreateAdhocTaskRequest;
import com.nivasafinance.features.leadtasks.dto.LeadCompleteTaskRequest;
import com.nivasafinance.features.leadtasks.dto.LeadReassignTaskRequest;
import com.nivasafinance.features.leadtasks.dto.LeadRescheduleTaskRequest;
import com.nivasafinance.features.leadtasks.dto.LeadTaskResponse;
import com.nivasafinance.features.leadtasks.entity.LeadTask;
import com.nivasafinance.features.leadtasks.exception.LeadTasksExceptionFactory;
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
import lombok.RequiredArgsConstructor;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class LeadTaskWriteServiceImpl implements LeadTaskWriteService {

    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final LeadTaskRepositoryWrapper leadTaskRepositoryWrapper;
    private final TaskWriteService taskWriteService;
    private final TaskRepositoryWrapper taskRepositoryWrapper;
    private final LeadWorkflowAdapter leadWorkflowAdapter;
    private final MessageSource messageSource;

    @Override
    public LeadTaskResponse createAdhocTask(UUID leadIdentifier, CreateAdhocTaskRequest request) {
        Lead lead = getLead(leadIdentifier);

        String stageKey = getCurrentStageKey(lead, request.getStageKey());

        CreateAdhocTaskRequest fullRequest = buildCreateAdhocTaskRequest(request, lead, stageKey);

        Object result = leadWorkflowAdapter.createAdhocTask(leadIdentifier, fullRequest);

        return (LeadTaskResponse) result;
    }

    @Override
    public LeadTaskResponse createTaskAndAssociateWithLead(Long leadId, CreateTaskRequest request,
            Map<String, Object> taskDetails) {
        TaskResponse taskResponse = taskWriteService.createTask(request);

        String stageKey = (String) taskDetails.get(WorkflowConstants.TaskDetails.STAGE_KEY);

        return LeadTaskResponse.from(
                leadTaskRepositoryWrapper.save(buildLeadTask(leadId, taskResponse.getId(), stageKey)),
                getLead(leadId).getLeadIdentifier(), taskResponse);
    }

    @Override
    public LeadTaskResponse completeTask(UUID leadIdentifier, LeadCompleteTaskRequest request) {
        Lead lead = getLead(leadIdentifier);

        CompleteTaskRequest completeTaskRequest = LeadCompleteTaskRequest.toCompleteTaskRequest(request);

        TaskResponse taskResponse = taskWriteService.completeTask(completeTaskRequest);

        LeadTask leadTask = leadTaskRepositoryWrapper.findByTaskId(taskResponse.getId())
                .orElseThrow(() -> LeadTasksExceptionFactory.leadTaskNotFound(taskResponse.getId(), lead.getId(),
                        messageSource));

        return LeadTaskResponse.from(leadTask, leadIdentifier, taskResponse);
    }

    @Override
    public LeadTaskResponse reassignTask(UUID leadIdentifier, LeadReassignTaskRequest request) {
        Lead lead = getLead(leadIdentifier);

        ReassignTaskRequest reassignTaskRequest = buildReassignTaskRequest(request);

        TaskResponse taskResponse = taskWriteService.reassignTask(reassignTaskRequest);

        LeadTask leadTask = leadTaskRepositoryWrapper.findByTaskId(taskResponse.getId())
                .orElseThrow(() -> LeadTasksExceptionFactory.leadTaskNotFound(taskResponse.getId(), lead.getId(),
                        messageSource));

        return LeadTaskResponse.from(leadTask, leadIdentifier, taskResponse);
    }

    @Override
    public LeadTaskResponse rescheduleTask(UUID leadIdentifier, LeadRescheduleTaskRequest request) {
        Lead lead = getLead(leadIdentifier);
        Task oldTask = getTask(request.getTaskIdentifier());

        UUID rescheduledFromTaskId = computeRescheduledFromTaskIdentifier(oldTask, request);
        RescheduleTaskRequest rescheduleTaskRequest = buildRescheduleTaskRequest(request, rescheduledFromTaskId);

        TaskResponse taskResponse = taskWriteService.rescheduleTask(rescheduleTaskRequest);

        if (shouldCreateFollowUpTask(oldTask)) {
            createFollowUpTaskForLead(oldTask, request, rescheduledFromTaskId, lead);
        }

        LeadTask leadTask = findLeadTaskByTaskId(taskResponse.getId(), lead.getId());
        return LeadTaskResponse.from(leadTask, leadIdentifier, taskResponse);
    }

    private Task getTask(UUID taskIdentifier) {
        return taskRepositoryWrapper.findByTaskIdentifierWithException(taskIdentifier);
    }

    private UUID computeRescheduledFromTaskIdentifier(Task oldTask, LeadRescheduleTaskRequest request) {
        return ValidationUtils.isNonNull(request.getRescheduledFromTaskIdentifier())
                ? request.getRescheduledFromTaskIdentifier()
                : oldTask.getTaskIdentifier();
    }

    private RescheduleTaskRequest buildRescheduleTaskRequest(LeadRescheduleTaskRequest request, UUID rescheduledFromTaskId) {
        RescheduleTaskRequest rescheduleTaskRequest = LeadRescheduleTaskRequest.toRescheduleTaskRequest(request);
        rescheduleTaskRequest.setRescheduledFromTaskIdentifier(rescheduledFromTaskId);
        return rescheduleTaskRequest;
    }

    private boolean shouldCreateFollowUpTask(Task oldTask) {
        return ValidationUtils.isNonNull(oldTask.getTaskDetails())
                && ValidationUtils.isNonNull(oldTask.getTaskDetails().getEntityType())
                && ValidationUtils.isNonNull(oldTask.getTaskDetails().getEntityId())
                && oldTask.getTaskDetails().getEntityType() == EntityType.LEAD;
    }

    private void createFollowUpTaskForLead(Task oldTask, LeadRescheduleTaskRequest request, UUID rescheduledFromTaskId, Lead lead) {
        String rescheduledFromTaskRemarks = computeRescheduledFromTaskRemarks(oldTask);
        TaskDetailsRequest.PreferredCallWindow preferredCallWindow = LeadRescheduleTaskRequest.buildPreferredCallWindow(oldTask, request);
        CreateTaskRequest createTaskRequest = LeadRescheduleTaskRequest.buildCreateTaskRequest(
                oldTask, request, rescheduledFromTaskId, rescheduledFromTaskRemarks, preferredCallWindow);

        String stageKey = getStageKeyFromOldLeadTask(oldTask, lead.getLeadIdentifier());
        if (ValidationUtils.isNonNull(stageKey)) {
            createTaskAndAssociateWithLead(lead.getId(), createTaskRequest, buildTaskDetailsMap(stageKey));
        } else {
            taskWriteService.createTask(createTaskRequest);
        }
    }

    private String computeRescheduledFromTaskRemarks(Task oldTask) {
        if (!ValidationUtils.isNonNull(oldTask.getTaskDetails())) {
            return null;
        }
        if (ValidationUtils.isNonNullOrEmpty(oldTask.getTaskDetails().getRescheduledFromTaskRemarks())) {
            return oldTask.getTaskDetails().getRescheduledFromTaskRemarks();
        }
        if (ValidationUtils.isNonNullOrEmpty(oldTask.getTaskDetails().getCreatorRemarks())) {
            return oldTask.getTaskDetails().getCreatorRemarks();
        }
        return getRemarksFromPreviousTask(oldTask);
    }

    private String getRemarksFromPreviousTask(Task oldTask) {
        if (!ValidationUtils.isNonNull(oldTask.getTaskDetails().getRescheduledFromTaskIdentifier())) {
            return null;
        }
        try {
            Task previousTask = taskRepositoryWrapper.findByTaskIdentifierWithException(
                    oldTask.getTaskDetails().getRescheduledFromTaskIdentifier());
            if (ValidationUtils.isNonNull(previousTask)
                    && ValidationUtils.isNonNull(previousTask.getOutcomeDetails())
                    && ValidationUtils.isNonNullOrEmpty(previousTask.getOutcomeDetails().getRemarks())) {
                return previousTask.getOutcomeDetails().getRemarks();
            }
        } catch (Exception e) {
            // If previous task not found or any error, leave remarks as null
        }
        return null;
    }

    private String getStageKeyFromOldLeadTask(Task oldTask, UUID leadIdentifier) {
        LeadTask oldLeadTask = leadTaskRepositoryWrapper.findByTaskId(oldTask.getId())
                .orElseThrow(() -> LeadTasksExceptionFactory.invalidTaskDetails(oldTask.getId(), leadIdentifier, messageSource));
        return oldLeadTask.getTaskDetails() != null ? oldLeadTask.getTaskDetails().getStageKey() : null;
    }

    private Map<String, Object> buildTaskDetailsMap(String stageKey) {
        Map<String, Object> taskDetails = new java.util.HashMap<>();
        taskDetails.put(WorkflowConstants.TaskDetails.STAGE_KEY, stageKey);
        return taskDetails;
    }

    private LeadTask findLeadTaskByTaskId(Long taskId, Long leadId) {
        return leadTaskRepositoryWrapper.findByTaskId(taskId)
                .orElseThrow(() -> LeadTasksExceptionFactory.leadTaskNotFound(taskId, leadId, messageSource));
    }

    private Lead getLead(UUID leadIdentifier) {
        return leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
    }

    private Lead getLead(Long leadId) {
        return leadRepositoryWrapper.findByIdWithException(leadId);
    }

    private LeadTask buildLeadTask(Long leadId, Long taskId, String stageKey) {
        return LeadTask.builder()
                .leadId(leadId)
                .taskId(taskId)
                .taskDetails(LeadTask.TaskDetails.builder()
                        .stageKey(stageKey)
                        .build())
                .build();
    }

    private ReassignTaskRequest buildReassignTaskRequest(LeadReassignTaskRequest request) {
        return ReassignTaskRequest.builder()
                .taskIdentifier(request.getTaskIdentifier())
                .newAssignedTo(request.getNewAssignedTo())
                .build();
    }

    private String getCurrentStageKey(Lead lead, String providedStageKey) {
        if (ValidationUtils.isNonNull(providedStageKey)) {
            return providedStageKey;
        } 
        throw LeadTasksExceptionFactory.stageNotFound(messageSource);
    }

    private CreateAdhocTaskRequest buildCreateAdhocTaskRequest(CreateAdhocTaskRequest request, Lead lead,
            String stageKey) {
        return CreateAdhocTaskRequest.builder()
                .taskConfigKey(request.getTaskConfigKey())
                .assignedTo(request.getAssignedTo())
                .dueAt(request.getDueAt())
                .stageKey(stageKey)
                .creatorRemarks(request.getCreatorRemarks())
                .preferredCallWindowStart(request.getPreferredCallWindowStart())
                .preferredCallWindowEnd(request.getPreferredCallWindowEnd())
                .build();
    }
}