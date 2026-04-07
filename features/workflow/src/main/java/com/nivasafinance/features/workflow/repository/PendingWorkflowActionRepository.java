package com.nivasafinance.features.workflow.repository;

import com.nivasafinance.features.workflow.entity.PendingWorkflowAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PendingWorkflowActionRepository extends JpaRepository<PendingWorkflowAction, Long> {

    List<PendingWorkflowAction> findBySourceTaskIdentifierAndStatus(UUID sourceTaskIdentifier, String status);

    List<PendingWorkflowAction> findByEntityIdentifierAndEntityTypeAndStatus(
            UUID entityIdentifier, com.nivasafinance.common.enums.EntityType entityType, String status);

    Optional<PendingWorkflowAction> findByActionIdentifier(UUID actionIdentifier);
}
