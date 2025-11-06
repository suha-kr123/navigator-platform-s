package com.nivasafinance.features.task.repository;

import com.nivasafinance.features.task.entity.TaskConfig;
import com.nivasafinance.features.task.exception.TaskConfigNotFoundException;
import com.nivasafinance.features.task.exception.TaskOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskConfigRepositoryWrapper {

    private final TaskConfigRepository taskConfigRepository;
    private final MessageSource messageSource;

    public TaskConfig findActiveByTaskConfigKey(String taskConfigKey) {
        TaskConfig taskConfig = taskConfigRepository.findByTaskConfigKey(taskConfigKey)
                .orElseThrow(() -> new TaskConfigNotFoundException(taskConfigKey, messageSource));

        if (!Boolean.TRUE.equals(taskConfig.getIsActive())) {
            throw TaskOperationException.taskConfigInactive(taskConfigKey, messageSource);
        }

        return taskConfig;
    }
}

