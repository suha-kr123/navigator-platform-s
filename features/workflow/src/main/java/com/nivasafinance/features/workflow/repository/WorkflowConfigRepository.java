package com.nivasafinance.features.workflow.repository;

import com.nivasafinance.features.workflow.entity.WorkflowConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkflowConfigRepository extends JpaRepository<WorkflowConfig, Long> {
    Optional<WorkflowConfig> findByWorkflowConfigKey(String workflowConfigKey);
}

