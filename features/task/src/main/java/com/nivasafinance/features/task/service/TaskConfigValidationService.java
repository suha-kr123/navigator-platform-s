package com.nivasafinance.features.task.service;

import com.nivasafinance.features.task.entity.TaskConfig;

public interface TaskConfigValidationService {

    void validateOutcome(TaskConfig taskConfig, String outcome);

    void validateRescheduleAllowed(TaskConfig taskConfig);
}

