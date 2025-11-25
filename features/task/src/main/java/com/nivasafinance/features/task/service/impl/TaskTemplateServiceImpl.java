package com.nivasafinance.features.task.service.impl;

import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeMasterService;
import com.nivasafinance.features.task.dto.TaskTemplateResponse;
import com.nivasafinance.features.task.entity.TaskConfig;
import com.nivasafinance.features.task.repository.TaskConfigRepositoryWrapper;
import com.nivasafinance.features.task.service.TaskTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskTemplateServiceImpl implements TaskTemplateService {

    private final TaskConfigRepositoryWrapper taskConfigRepositoryWrapper;
    private final CodeMasterService codeMasterService;

    @Override
    public TaskTemplateResponse getTaskTemplate(String taskConfigKey, String officeKey) {
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
}

