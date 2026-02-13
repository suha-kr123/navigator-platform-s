package com.nivasafinance.features.referral.service;

import com.nivasafinance.features.referral.dto.ReferralCodeRegistryResponse;

import java.util.UUID;
import com.nivasafinance.features.referral.enums.EntityType;

public interface ReferralCodeRegistryService {

    ReferralCodeRegistryResponse generateReferralCode(EntityType entityType, UUID entityIdentifier);

    ReferralCodeRegistryResponse getReferralCodeByCode(String referralCode);

    ReferralCodeRegistryResponse getReferralCodeByEntity(EntityType entityType, UUID entityIdentifier);

}