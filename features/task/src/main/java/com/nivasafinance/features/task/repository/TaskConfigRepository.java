package com.nivasafinance.features.task.repository;

import java.util.List;
import java.util.Optional;

import com.nivasafinance.features.task.entity.TaskConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;

@Repository
public interface TaskConfigRepository extends JpaRepository<TaskConfig, Long> {
    
    Optional<TaskConfig> findByTaskConfigKey(String taskConfigKey);
    
    List<TaskConfig> findByTaskConfigKeyInAndIsActiveTrue(List<String> taskConfigKeys);

    @Query(value = "SELECT * FROM n_task_config tc " +
            "WHERE COALESCE(tc.task_config_details->>'isAdhocTaskAllowed', 'false') = 'true' " +
            "AND tc.is_active = true", nativeQuery = true)
    List<TaskConfig> findActiveAdhocTasks();

}   
