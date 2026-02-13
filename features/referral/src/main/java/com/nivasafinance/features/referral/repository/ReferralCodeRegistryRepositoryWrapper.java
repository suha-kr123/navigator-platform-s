package com.nivasafinance.features.referral.repository;

import com.nivasafinance.features.referral.entity.ReferralCodeRegistry;
import com.nivasafinance.features.referral.enums.EntityType;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ReferralCodeRegistryRepositoryWrapper {

    private final ReferralCodeRegistryRepository referralCodeRegistryRepository;

    public ReferralCodeRegistryRepositoryWrapper(ReferralCodeRegistryRepository referralCodeRegistryRepository) {
        this.referralCodeRegistryRepository = referralCodeRegistryRepository;
    }

    public ReferralCodeRegistry save(ReferralCodeRegistry registry) {
        try {
            return referralCodeRegistryRepository.save(registry);
        } catch (DataAccessException ex) {
            throw new RuntimeException("Failed to save referral code registry", ex);
        }
    }

    public Optional<ReferralCodeRegistry> findByCode(String referralCode) {
        try {
            return referralCodeRegistryRepository.findByReferredByCode(referralCode);
        } catch (DataAccessException ex) {
            throw new RuntimeException("Failed to fetch referral code by code", ex);
        }
    }

    public Optional<ReferralCodeRegistry> findByEntity(EntityType entityType, UUID entityIdentifier) {
        return referralCodeRegistryRepository.findByEntityTypeAndEntityIdentifier(entityType, entityIdentifier);
    }

    public boolean existsByCode(String referralCode) {
        try {
            return referralCodeRegistryRepository.existsByReferredByCode(referralCode);
        } catch (DataAccessException ex) {
            throw new RuntimeException("Failed to check referral code existence", ex);
        }
    }
}
