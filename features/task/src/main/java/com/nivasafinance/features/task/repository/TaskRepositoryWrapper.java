package com.nivasafinance.features.task.repository;

import com.nivasafinance.features.task.entity.Task;
import com.nivasafinance.features.task.exception.TaskNotFoundException;
import com.nivasafinance.features.task.exception.TaskOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskRepositoryWrapper {

    private final TaskRepository taskRepository;
    private final MessageSource messageSource;

    public Task saveWithException(Task task) {
        try {
            return taskRepository.save(task);
        } catch (RuntimeException e) {
            log.error("Error saving task", e);
            throw TaskOperationException.taskConfigInactive(task.getTaskConfigKey(), messageSource);
        }
    }

    public Task findByTaskIdentifierWithException(String taskIdentifier) {
        return taskRepository.findByTaskIdentifier(taskIdentifier)
                .orElseThrow(() -> TaskNotFoundException.taskNotFound(taskIdentifier, messageSource));
    }

    public Optional<Task> findById(Long id) {
        return taskRepository.findById(id);
    }
}

