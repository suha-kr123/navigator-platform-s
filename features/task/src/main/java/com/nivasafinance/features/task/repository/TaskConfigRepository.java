package com.nivasafinance.features.task.repository;

import com.nivasafinance.features.task.entity.TaskConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskConfigRepository extends JpaRepository<TaskConfig, Long> {
    
    Optional<TaskConfig> findByTaskConfigKey(String taskConfigKey);
    
    List<TaskConfig> findByIsActive(Boolean isActive);
    
    boolean existsByTaskConfigKey(String taskConfigKey);
}

