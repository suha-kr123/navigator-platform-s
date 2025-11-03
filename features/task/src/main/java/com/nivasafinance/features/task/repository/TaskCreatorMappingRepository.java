package com.nivasafinance.features.task.repository;

import com.nivasafinance.features.task.entity.TaskCreatorMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskCreatorMappingRepository extends JpaRepository<TaskCreatorMapping, Long> {
    
    List<TaskCreatorMapping> findByTaskConfigKey(String taskConfigKey);
    
    List<TaskCreatorMapping> findByRoleKey(String roleKey);
    
    boolean existsByTaskConfigKeyAndRoleKey(String taskConfigKey, String roleKey);
}

