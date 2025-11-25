package com.nivasafinance.features.leadtasks.repository;

import com.nivasafinance.features.leadtasks.entity.LeadTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeadTaskRepository extends JpaRepository<LeadTask, Long> {
    List<LeadTask> findByLeadId(Long leadId);
    Page<LeadTask> findByLeadIdOrderByCreatedAtDesc(Long leadId, Pageable pageable);
    List<LeadTask> findByTaskIdIn(List<Long> taskIds);
    Optional<LeadTask> findByTaskId(Long taskId);
}

