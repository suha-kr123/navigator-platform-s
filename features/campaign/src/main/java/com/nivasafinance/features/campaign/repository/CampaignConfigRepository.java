package com.nivasafinance.features.campaign.repository;

import com.nivasafinance.features.campaign.entity.CampaignConfig;
import com.nivasafinance.features.campaign.enums.CampaignConfigStatus;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@JaversSpringDataAuditable
public interface CampaignConfigRepository extends JpaRepository<CampaignConfig, Long> {
    Optional<CampaignConfig> findByIdentifier(UUID identifier);
    Page<CampaignConfig> findByStatusIn(List<CampaignConfigStatus> statuses, Pageable pageable);
}

