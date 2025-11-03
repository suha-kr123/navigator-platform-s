package com.nivasafinance.features.task.repository;

import com.nivasafinance.features.task.entity.TaskConfig;
import com.nivasafinance.features.task.exception.TaskConfigNotFoundException;
import com.nivasafinance.features.task.exception.TaskOperationException;
import com.nivasafinance.features.task.exception.TaskValidationException;
import com.nivasafinance.common.utils.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskConfigRepositoryWrapper {

    private final TaskConfigRepository taskConfigRepository;
    private final MessageSource messageSource;

    /**
     * Finds TaskConfig by config key and validates it's active
     * @param taskConfigKey the task config key
     * @return Active TaskConfig
     * @throws TaskConfigNotFoundException if config not found
     * @throws TaskOperationException if config is inactive
     */
    public TaskConfig findActiveByTaskConfigKey(String taskConfigKey) {
        TaskConfig taskConfig = taskConfigRepository.findByTaskConfigKey(taskConfigKey)
                .orElseThrow(() -> new TaskConfigNotFoundException(taskConfigKey, messageSource));

        if (!Boolean.TRUE.equals(taskConfig.getIsActive())) {
            throw TaskOperationException.taskConfigInactive(taskConfigKey, messageSource);
        }

        return taskConfig;
    }

    /**
     * Validates if the given outcome is allowed for this task config
     * @param taskConfig the task config to validate against
     * @param outcome the outcome to validate
     * @throws TaskValidationException if outcome is not valid
     */
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

    /**
     * Validates if rescheduling is allowed for this task config
     * @param taskConfig the task config to validate against
     * @throws TaskOperationException if rescheduling is not allowed
     */
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

