package com.nivasafinance.features.referral.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nivasafinance.features.referral.entity.ReferralCodeRegistry;
import com.nivasafinance.features.referral.enums.EntityType;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReferralCodeRegistryRepository extends JpaRepository<ReferralCodeRegistry, Long> {

    Optional<ReferralCodeRegistry> findByReferredByCode(String referredByCode);

    Optional<ReferralCodeRegistry> findByEntityTypeAndEntityIdentifier(EntityType entityType, UUID entityIdentifier);

    boolean existsByReferredByCode(String referredByCode);
}
