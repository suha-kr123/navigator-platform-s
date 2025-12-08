package com.nivasafinance.features.task.repository;

import com.nivasafinance.features.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    Optional<Task> findByTaskIdentifier(UUID taskIdentifier);
    
    @Query(value = "SELECT * FROM n_tasks t " +
           "WHERE (t.task_details->>'entityId')::uuid = :entityId " +
           "AND t.task_details->>'entityType' = :entityType " +
           "AND t.outcome IS NULL", nativeQuery = true)
    List<Task> findOpenTasksByEntityIdAndEntityType(
            @Param("entityId") UUID entityId,
            @Param("entityType") String entityType
    );
}

