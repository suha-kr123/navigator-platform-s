package com.nivasafinance.features.lead.task;

import java.util.UUID;
import java.util.List;

import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.lead.dto.LeadResponse;
import com.nivasafinance.features.lead.service.LeadReadService;
import com.nivasafinance.features.task.dto.TaskDetailsRequest;
import com.nivasafinance.features.task.service.TaskEntityService;
import com.nivasafinance.features.workflow.service.WorkflowConfigReadService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LeadEntityService implements TaskEntityService {

    private final WorkflowConfigReadService workflowConfigReadService;
    private final LeadReadService leadReadService;

    @Override
    public EntityType getEntityType() {
        return EntityType.LEAD;
    }

    @Override
    public void validate(UUID entityId) {
        leadReadService.getLeadByIdentifier(entityId);
    }

    @Override
    public boolean canCreateAdhocTask(TaskDetailsRequest taskDetails, String taskConfigKey) {
        LeadResponse leadResponse = leadReadService.getLeadByIdentifier(taskDetails.getEntityId());
        boolean hasStageKey = ValidationUtils.isNonNullOrEmpty(taskDetails.getStageKey());
        if (hasStageKey) {
            return workflowConfigReadService.canCreateAdhocTask(leadResponse.getWorkflowConfigKey(),
                    taskConfigKey,
                    taskDetails.getStageKey());
        }
        return true;
    }

    @Override
    public List<String> getAdhocTasksTemplates(UUID entityId) {
        LeadResponse leadResponse = leadReadService.getLeadByIdentifier(entityId);
        List<String> adhocTaskKeys = workflowConfigReadService.getAdhocTaskKeysForStage(leadResponse.getWorkflowConfigKey(), leadResponse.getCurrentStageKey());
        return adhocTaskKeys;
    }
}
