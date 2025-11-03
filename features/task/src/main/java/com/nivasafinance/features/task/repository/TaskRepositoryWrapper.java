package com.nivasafinance.features.task.repository;

import com.nivasafinance.features.task.entity.Task;
import com.nivasafinance.features.task.exception.TaskNotFoundException;
import com.nivasafinance.features.task.exception.TaskOperationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskRepositoryWrapper {

    private final TaskRepository taskRepository;
    private final MessageSource messageSource;

    /**
     * Saves task with exception handling
     * @param task the task to save
     * @return saved Task
     * @throws TaskOperationException if save fails
     */
    public Task saveWithException(Task task) {
        try {
            return taskRepository.save(task);
        } catch (RuntimeException e) {
            log.error("Error saving task", e);
            throw TaskOperationException.taskConfigInactive(task.getTaskConfigKey(), messageSource);
        }
    }

    /**
     * Finds task by ID
     * @param taskId the task ID
     * @return Task
     * @throws TaskNotFoundException if task not found
     */
    public Task findByIdWithException(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId, messageSource));
    }
}

