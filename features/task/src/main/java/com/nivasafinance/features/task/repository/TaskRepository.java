package com.nivasafinance.features.task.repository;

import com.nivasafinance.features.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    Optional<Task> findByTaskIdentifier(UUID taskIdentifier);
}

