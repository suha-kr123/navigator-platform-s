package com.nivasafinance.features.task.repository;

import java.util.Optional;

import com.nivasafinance.features.task.entity.TaskConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskConfigRepository extends JpaRepository<TaskConfig, Long> {
    
    Optional<TaskConfig> findByTaskConfigKey(String taskConfigKey);

}   

