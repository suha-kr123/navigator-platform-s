package com.nivasafinance.features.campaign.repository;

import com.nivasafinance.features.campaign.entity.Campaign;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@JaversSpringDataAuditable
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    Optional<Campaign> findByIdentifier(UUID identifier);
    
    Optional<Campaign> findByProviderId(String providerId);
    
    boolean existsByName(String name);
}

