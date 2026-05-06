package com.nivasafinance.features.workflow.service.impl;

import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.workflow.dto.WorkflowStageConfig;
import com.nivasafinance.features.workflow.entity.WorkflowConfig;
import com.nivasafinance.features.workflow.exception.WorkflowValidationException;
import com.nivasafinance.features.workflow.repository.WorkflowConfigRepositoryWrapper;
import com.nivasafinance.features.workflow.service.WorkflowConfigReadService;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WorkflowConfigReadServiceImpl implements WorkflowConfigReadService {

    private final WorkflowConfigRepositoryWrapper workflowConfigRepositoryWrapper;

    @Override
    public WorkflowConfig getWorkflowConfigByKey(String workflowConfigKey) {
        return workflowConfigRepositoryWrapper.findActiveByWorkflowConfigKey(workflowConfigKey);
    }

    @Override
    public boolean canCreateAdhocTask(String workflowConfigKey, String taskConfigKey, String stageKey) {
        ValidationUtils.requireNonNullOrEmpty(workflowConfigKey,
                WorkflowValidationException::nullOrEmptyWorkflowConfigKey);
        ValidationUtils.requireNonNullOrEmpty(taskConfigKey, WorkflowValidationException::nullOrEmptyTaskConfigKey);
        ValidationUtils.requireNonNullOrEmpty(stageKey, WorkflowValidationException::nullOrEmptyStageKey);
        WorkflowConfig workflowConfig = getWorkflowConfigByKey(workflowConfigKey);
        if (workflowConfig == null) {
            return false;
        }
        WorkflowStageConfig workflowStageConfig = workflowConfig.getWorkflowConfigDetails().getStages().stream()
                .filter(stage -> stageKey.equals(stage.getStageKey()))
                .findFirst().orElse(null);
        if (workflowStageConfig == null) {
            return false;
        }
        return workflowStageConfig.getAllowedAdhocTasks().contains(taskConfigKey);
    }

    @Override
    public List<String> getAdhocTaskKeysForStage(String workflowConfigKey, String stageKey) {
        // Input validation
        ValidationUtils.requireNonNullOrEmpty(workflowConfigKey, WorkflowValidationException::nullOrEmptyWorkflowConfigKey);
        ValidationUtils.requireNonNullOrEmpty(stageKey, WorkflowValidationException::nullOrEmptyStageKey);
        com.nivasafinance.features.workflow.entity.WorkflowConfig workflowConfig = getWorkflowConfigByKey(workflowConfigKey);

        if (!ValidationUtils.isNonNull(workflowConfig) 
                || !ValidationUtils.isNonNull(workflowConfig.getWorkflowConfigDetails())
                || !ValidationUtils.isNonNull(workflowConfig.getWorkflowConfigDetails().getStages())) {
            return new ArrayList<>();
        }

        Optional<WorkflowStageConfig> stageConfigOpt = workflowConfig.getWorkflowConfigDetails().getStages().stream()
                .filter(stage -> stageKey.equals(stage.getStageKey()))
                .findFirst();

        if (stageConfigOpt.isEmpty()) {
            return new ArrayList<>();
        }

        WorkflowStageConfig stageConfig = stageConfigOpt.get();
        List<String> allowedAdhocTaskKeys = stageConfig.getAllowedAdhocTasks();

        if (!ValidationUtils.isNonNull(allowedAdhocTaskKeys) || allowedAdhocTaskKeys.isEmpty()) {
            return new ArrayList<>();
        }

        return allowedAdhocTaskKeys;
    }

    @Override
    public String getStageIdentifier(String workflowConfigKey, String stageKey) {
        ValidationUtils.requireNonNullOrEmpty(workflowConfigKey, WorkflowValidationException::nullOrEmptyWorkflowConfigKey);
        ValidationUtils.requireNonNullOrEmpty(stageKey, WorkflowValidationException::nullOrEmptyStageKey);

        WorkflowConfig workflowConfig = getWorkflowConfigByKey(workflowConfigKey);

        if (!ValidationUtils.isNonNull(workflowConfig)
                || !ValidationUtils.isNonNull(workflowConfig.getWorkflowConfigDetails())
                || !ValidationUtils.isNonNull(workflowConfig.getWorkflowConfigDetails().getStages())) {
            return null;
        }

        return workflowConfig.getWorkflowConfigDetails().getStages().stream()
                .filter(stage -> stageKey.equals(stage.getStageKey()))
                .findFirst()
                .map(WorkflowStageConfig::getIdentifier)
                .orElse(null);
    }

}

