package com.nivasafinance.features.task.service.impl;

import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeMasterService;
import com.nivasafinance.features.task.entity.TaskConfig;
import com.nivasafinance.features.task.exception.TaskOperationException;
import com.nivasafinance.features.task.exception.TaskValidationException;
import com.nivasafinance.features.task.service.TaskConfigValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskConfigValidationServiceImpl implements TaskConfigValidationService {

    private final MessageSource messageSource;
    private final CodeMasterService codeMasterService;

    @Override
    public void validateOutcome(TaskConfig taskConfig, String outcome) {
        TaskConfig.TaskConfigDetails configDetails = taskConfig.getTaskConfigDetails();
        if (ValidationUtils.isNonNull(configDetails) && ValidationUtils.isNonNullOrEmpty(configDetails.getAllowedOutcomesCodeValueKey())) {
            String codeValueKey = configDetails.getAllowedOutcomesCodeValueKey();
            List<CodeValueResponse> allowedOutcomes = codeMasterService.getAllCodeValuesByCodeKey(codeValueKey, true, "default");
            List<String> allowedOutcomeKeys = allowedOutcomes.stream()
                    .map(CodeValueResponse::getKey)
                    .collect(Collectors.toList());
            
            if (!allowedOutcomeKeys.contains(outcome)) {
                throw TaskValidationException.invalidOutcome(outcome, taskConfig.getTaskConfigKey(), messageSource);
            }
        }
    }

    @Override
    public void validateRescheduleAllowed(TaskConfig taskConfig) {
        TaskConfig.TaskConfigDetails configDetails = taskConfig.getTaskConfigDetails();
        if (ValidationUtils.isNonNull(configDetails) && configDetails.getIsRescheduledAllowed() != null 
                && !configDetails.getIsRescheduledAllowed()) {
            throw TaskOperationException.rescheduleNotAllowed(taskConfig.getTaskConfigKey(), messageSource);
        }
    }
}

