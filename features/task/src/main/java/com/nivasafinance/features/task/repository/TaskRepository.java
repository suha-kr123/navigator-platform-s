package com.nivasafinance.features.task.repository;

import com.nivasafinance.features.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    List<Task> findByTaskConfigKey(String taskConfigKey);
    
    List<Task> findByAssignedTo(String assignedTo);
    
    List<Task> findByAssignedToRole(String assignedToRole);
    
    List<Task> findByOutcome(String outcome);
    
    List<Task> findByTaskConfigKeyAndOutcomeIsNull(String taskConfigKey);
    
    Optional<Task> findByIdAndAssignedTo(Long id, String assignedTo);
}

