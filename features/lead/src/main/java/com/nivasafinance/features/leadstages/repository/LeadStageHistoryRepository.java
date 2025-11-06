package com.nivasafinance.features.leadstages.repository;

import com.nivasafinance.features.leadstages.entity.LeadStageHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LeadStageHistoryRepository extends JpaRepository<LeadStageHistory, Long> {
    Optional<LeadStageHistory> findFirstByLeadIdOrderByEnteredAtDesc(Long leadId);
    Page<LeadStageHistory> findByLeadIdOrderByEnteredAtDesc(Long leadId, Pageable pageable);
}

