package com.nivasafinance.features.advisorlead.repository;

import com.nivasafinance.features.advisorlead.entity.AdvisorLeadMapping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AdvisorLeadMappingRepository extends JpaRepository<AdvisorLeadMapping, UUID> {
    Page<AdvisorLeadMapping> findAllByAdvisorId(UUID advisorId, Pageable pageable);
}

