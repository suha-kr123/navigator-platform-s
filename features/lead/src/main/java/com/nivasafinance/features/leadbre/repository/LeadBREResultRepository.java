package com.nivasafinance.features.leadbre.repository;

import com.nivasafinance.features.leadbre.entity.LeadBREResult;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@JaversSpringDataAuditable
public interface LeadBREResultRepository extends JpaRepository<LeadBREResult, Long> {

    Optional<LeadBREResult> findByIdentifier(UUID identifier);

    List<LeadBREResult> findByLeadIdOrderByCreatedAtDesc(Long leadId);

    List<LeadBREResult> findByLeadIdAndConfigNameOrderByCreatedAtDesc(Long leadId, String configName);

    Optional<LeadBREResult> findTopByLeadIdAndConfigNameOrderByCreatedAtDesc(Long leadId, String configName);
}
