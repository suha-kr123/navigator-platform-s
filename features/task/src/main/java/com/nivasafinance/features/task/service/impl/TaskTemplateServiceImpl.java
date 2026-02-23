package com.nivasafinance.features.task.service.impl;

import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeMasterService;
import com.nivasafinance.features.rolemanagement.role.dto.UserAssignmentResponse;
import com.nivasafinance.features.rolemanagement.role.service.EntityOfficeKeyService;
import com.nivasafinance.features.rolemanagement.role.service.UserQueryService;
import com.nivasafinance.features.task.dto.TaskTemplateResponse;
import com.nivasafinance.features.task.entity.TaskConfig;
import com.nivasafinance.features.task.repository.TaskConfigRepositoryWrapper;
import com.nivasafinance.features.task.service.TaskEntityService;
import com.nivasafinance.features.task.service.TaskEntityServiceFactory;
import com.nivasafinance.features.task.service.TaskTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskTemplateServiceImpl implements TaskTemplateService {

    private final TaskConfigRepositoryWrapper taskConfigRepositoryWrapper;
    private final CodeMasterService codeMasterService;
    private final TaskEntityServiceFactory taskEntityServiceFactory;
    private final EntityOfficeKeyService entityOfficeKeyService;
    private final UserQueryService userQueryService;

    @Override
    public TaskTemplateResponse getTaskTemplate(String taskConfigKey) {
        TaskConfig taskConfig = taskConfigRepositoryWrapper.findActiveByTaskConfigKey(taskConfigKey);
        
        TaskConfig.TaskConfigDetails taskConfigDetails = taskConfig.getTaskConfigDetails();
        List<String> allowedRoles = Collections.emptyList();
        List<CodeValueResponse> allowedOutcomes = Collections.emptyList();
        Boolean isRescheduledAllowed = null;
        List<CodeValueResponse> rescheduleReasons = Collections.emptyList();
        
        if (ValidationUtils.isNonNull(taskConfigDetails)) {
            allowedRoles = ValidationUtils.isNonNull(taskConfigDetails.getAllowedRoles()) 
                    ? taskConfigDetails.getAllowedRoles() 
                    : Collections.emptyList();
            
            isRescheduledAllowed = taskConfigDetails.getIsRescheduledAllowed();
            
            if (ValidationUtils.isNonNull(taskConfigDetails.getAllowedOutcomesCodeValueKey())) {
                allowedOutcomes = codeMasterService.getAllCodeValuesByCodeKey(
                        taskConfigDetails.getAllowedOutcomesCodeValueKey(), true);
            }
            
            if (ValidationUtils.isNonNull(taskConfigDetails.getRescheduleReasonsCodeValueKey())) {
                rescheduleReasons = codeMasterService.getAllCodeValuesByCodeKey(
                        taskConfigDetails.getRescheduleReasonsCodeValueKey(), true);
            }
        }
        
        return TaskTemplateResponse.builder()
                .taskConfigKey(taskConfig.getTaskConfigKey())
                .taskName(taskConfig.getName())
                .taskDescription(taskConfig.getDescription())
                .allowedRoles(allowedRoles)
                .allowedOutcomes(allowedOutcomes)
                .isRescheduledAllowed(isRescheduledAllowed)
                .rescheduleReasons(rescheduleReasons)
                .build();
    }

    @Override
    public List<TaskTemplateResponse> getAdhocTasksTemplates(EntityType entityType, UUID entityId) {

        if (ValidationUtils.isNonNull(entityType) && ValidationUtils.isNonNull(entityId)) {

            TaskEntityService taskEntityService = taskEntityServiceFactory.getTaskEntityService(entityType);

            List<String> taskConfigKeys = Optional
                    .ofNullable(taskEntityService.getAdhocTasksTemplates(entityId))
                    .orElse(Collections.emptyList());

            return taskConfigKeys.stream()
                    .map(this::getTaskTemplate)
                    .collect(Collectors.toList());
        }

        return taskConfigRepositoryWrapper.findActiveAdhocTasks()
                .stream()
                .map(taskConfig -> getTaskTemplate(taskConfig.getTaskConfigKey()))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserAssignmentResponse> getAssignableUsersForTask(String taskConfigKey, EntityType entityType, UUID entityId) {
        TaskConfig taskConfig = taskConfigRepositoryWrapper.findActiveByTaskConfigKey(taskConfigKey);
        List<String> allowedRoles = ValidationUtils.isNonNull(taskConfig.getTaskConfigDetails())
                ? ValidationUtils.isNonNull(taskConfig.getTaskConfigDetails().getAllowedRoles())
                        ? taskConfig.getTaskConfigDetails().getAllowedRoles()
                        : Collections.emptyList()
                : Collections.emptyList();

        if (ValidationUtils.isNonNull(entityType) && ValidationUtils.isNonNull(entityId)) {
            String officeKey = entityOfficeKeyService.getOfficeKey(entityType, entityId);
            if (!ValidationUtils.isNonNull(officeKey)) {
                return Collections.emptyList();
            }
            return userQueryService.getUsersByOfficeAndRoles(allowedRoles, officeKey);
        }
        return userQueryService.getUsersByRoles(allowedRoles);
    }

}

