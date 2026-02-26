package com.nivasafinance.features.lender.lender.service.impl;

import com.nivasafinance.features.lender.lender.dto.LenderRequestData;
import com.nivasafinance.features.lender.lender.dto.LenderResponseData;
import com.nivasafinance.features.lender.lender.dto.UpdateLenderRequest;
import com.nivasafinance.features.lender.lender.entity.Lender;
import com.nivasafinance.features.lender.lender.enums.LenderStatus;
import com.nivasafinance.features.lender.lender.exception.LenderExceptionFactory;
import com.nivasafinance.features.lender.lender.repository.LenderRepositoryWrapper;
import com.nivasafinance.features.lender.lender.service.LenderWriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class LenderWriteServiceImpl implements LenderWriteService {

    private final LenderRepositoryWrapper lenderRepositoryWrapper;
    private final MessageSource messageSource;

    @Autowired
    public LenderWriteServiceImpl(LenderRepositoryWrapper lenderRepositoryWrapper, MessageSource messageSource) {
        this.lenderRepositoryWrapper = lenderRepositoryWrapper;
        this.messageSource = messageSource;
    }

    @Override
    public LenderResponseData create(LenderRequestData lenderData) {
        String name = lenderData.getName();
        if (name == null || name.isBlank()) {
            throw LenderExceptionFactory.createFailed(messageSource);
        }
        Set<String> existingKeys = lenderRepositoryWrapper.findAll().stream()
                .map(Lender::getKey)
                .collect(Collectors.toSet());
        String key = generateUniqueLenderKey(normalizeKeyPart(name), existingKeys);

        Lender lender = new Lender();
        lender.setKey(key);
        lender.setName(name);
        lender.setStatus(lenderData.getStatus() != null ? lenderData.getStatus() : LenderStatus.ACTIVE);

        Lender savedLender = lenderRepositoryWrapper.saveWithException(lender);
        return toResponse(savedLender);
    }

    private static String normalizeKeyPart(String value) {
        return value.trim()
                .toUpperCase()
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
    }

    private static String generateUniqueLenderKey(String normalizedBase, Set<String> existingKeys) {
        if (normalizedBase.isEmpty()) {
            normalizedBase = "LENDER";
        }
        if (existingKeys == null || !existingKeys.contains(normalizedBase)) {
            return normalizedBase;
        }
        int counter = 1;
        String candidate;
        do {
            candidate = normalizedBase + "_" + counter;
            counter++;
        } while (existingKeys.contains(candidate));
        return candidate;
    }

    @Override
    public LenderResponseData update(UUID id, UpdateLenderRequest request) {
        Lender existingLender = lenderRepositoryWrapper.findByIdWithException(id);
        existingLender.setName(request.getName());
        Lender savedLender = lenderRepositoryWrapper.saveWithException(existingLender);
        return toResponse(savedLender);
    }

    @Override
    public void delete(UUID id) {
        lenderRepositoryWrapper.deleteByIdWithException(id);
    }

    @Override
    public LenderResponseData activateDeactivateLender(UUID id) {
        Lender lender = lenderRepositoryWrapper.findByIdWithException(id);
        lender.setStatus(lender.getStatus() == LenderStatus.ACTIVE ? LenderStatus.INACTIVE : LenderStatus.ACTIVE);
        Lender saved = lenderRepositoryWrapper.saveWithException(lender);
        return toResponse(saved);
    }

    private LenderResponseData toResponse(Lender lender) {
        if (lender.getId() == null) {
            throw new IllegalStateException("Lender ID cannot be null");
        }
        return new LenderResponseData(
                lender.getId(),
                lender.getName(),
                lender.getKey(),
                lender.getStatus()
        );
    }
}

