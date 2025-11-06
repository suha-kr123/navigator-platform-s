package com.nivasafinance.features.task.service.impl;

import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.task.entity.TaskConfig;
import com.nivasafinance.features.task.exception.TaskOperationException;
import com.nivasafinance.features.task.exception.TaskValidationException;
import com.nivasafinance.features.task.service.TaskConfigValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TaskConfigValidationServiceImpl implements TaskConfigValidationService {

    private final MessageSource messageSource;

    @Override
    public void validateOutcome(TaskConfig taskConfig, String outcome) {
        Map<String, Object> configDetails = taskConfig.getTaskConfigDetails();
        if (ValidationUtils.isNonNull(configDetails) && configDetails.containsKey("allowedOutcomes")) {
            Object allowedOutcomes = configDetails.get("allowedOutcomes");
            if (allowedOutcomes instanceof List) {
                if (!((List<?>) allowedOutcomes).contains(outcome)) {
                    throw TaskValidationException.invalidOutcome(outcome, taskConfig.getTaskConfigKey(), messageSource);
                }
            }
        }
    }

    @Override
    public void validateRescheduleAllowed(TaskConfig taskConfig) {
        Map<String, Object> configDetails = taskConfig.getTaskConfigDetails();
        if (ValidationUtils.isNonNull(configDetails)) {
            Object isRescheduledAllowed = configDetails.get("isRescheduledAllowed");
            if (isRescheduledAllowed instanceof Boolean && !(Boolean) isRescheduledAllowed) {
                throw TaskOperationException.rescheduleNotAllowed(taskConfig.getTaskConfigKey(), messageSource);
            }
        }
    }
}

