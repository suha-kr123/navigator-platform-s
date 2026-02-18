package com.nivasafinance.features.referral.service.impl;

import com.nivasafinance.features.referral.dto.ReferralCodeRegistryResponse;
import com.nivasafinance.features.referral.entity.ReferralCodeRegistry;
import com.nivasafinance.features.referral.enums.EntityType;
import com.nivasafinance.features.referral.repository.ReferralCodeRegistryRepositoryWrapper;
import com.nivasafinance.features.referral.service.ReferralCodeRegistryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReferralCodeRegistryServiceImpl implements ReferralCodeRegistryService {

    private static final int CODE_LENGTH = 8;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Map<EntityType, String> PREFIX_MAP = Map.of(
        EntityType.ADVISOR, "ADV",
        EntityType.STAFF, "STA",
        EntityType.APPLICANT, "APP"
    );

    private final ReferralCodeRegistryRepositoryWrapper referralCodeRegistryRepositoryWrapper;

    @Override
    @Transactional
    public ReferralCodeRegistryResponse generateReferralCode(EntityType entityType, UUID entityIdentifier) {
        Optional<ReferralCodeRegistry> existing = referralCodeRegistryRepositoryWrapper.findByEntity(entityType, entityIdentifier);
        if (existing.isPresent()) {
            return toResponse(existing.get());
        }

        String code = generateUniqueCode(entityType);
        ReferralCodeRegistry registry = ReferralCodeRegistry.builder()
                .referredByCode(code)
                .entityType(entityType)
                .entityIdentifier(entityIdentifier)
                .build();
        ReferralCodeRegistry saved = referralCodeRegistryRepositoryWrapper.save(registry);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReferralCodeRegistryResponse getReferralCodeByCode(String referralCode) {
        return referralCodeRegistryRepositoryWrapper.findByCode(referralCode)
                .map(this::toResponse)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public ReferralCodeRegistryResponse getReferralCodeByEntity(EntityType entityType, UUID entityIdentifier) {
        return referralCodeRegistryRepositoryWrapper.findByEntity(entityType, entityIdentifier)
                .map(this::toResponse)
                .orElse(null);
    }

    private String generateUniqueCode(EntityType entityType) {
        String prefix = PREFIX_MAP.getOrDefault(entityType, "GEN");
        int randomPartLength = CODE_LENGTH - prefix.length(); // remaining length after prefix

        if (randomPartLength <= 0) {
            throw new IllegalArgumentException(
                    "Prefix length is too long for total CODE_LENGTH=" + CODE_LENGTH);
        }

        while (true) {
            String code = prefix + generateRandomPart(randomPartLength);
            if (!referralCodeRegistryRepositoryWrapper.existsByCode(code)) {
                return code;
            }
        }
    }

    private String generateRandomPart(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }

    private ReferralCodeRegistryResponse toResponse(ReferralCodeRegistry entity) {
        return ReferralCodeRegistryResponse.builder()
                .referralCode(entity.getReferredByCode())
                .entityType(entity.getEntityType())
                .entityIdentifier(entity.getEntityIdentifier())
                .build();
    }
}
