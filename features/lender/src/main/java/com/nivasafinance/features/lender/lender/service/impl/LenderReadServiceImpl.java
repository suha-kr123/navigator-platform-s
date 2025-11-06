package com.nivasafinance.features.lender.lender.service.impl;

import com.nivasafinance.features.lender.lender.dto.LenderResponseData;
import com.nivasafinance.features.lender.lender.entity.Lender;
import com.nivasafinance.features.lender.lender.enums.LenderStatus;
import com.nivasafinance.features.lender.lender.repository.LenderRepositoryWrapper;
import com.nivasafinance.features.lender.lender.service.LenderReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class LenderReadServiceImpl implements LenderReadService {

    private final LenderRepositoryWrapper lenderRepositoryWrapper;

    @Autowired
    public LenderReadServiceImpl(LenderRepositoryWrapper lenderRepositoryWrapper) {
        this.lenderRepositoryWrapper = lenderRepositoryWrapper;
    }

    @Override
    public LenderResponseData getById(UUID id) {
        Lender lender = lenderRepositoryWrapper.findByIdWithException(id);
        return toResponse(lender);
    }

    @Override
    public LenderResponseData getByKey(String key) {
        Lender lender = lenderRepositoryWrapper.findByKeyWithException(key);
        return toResponse(lender);
    }

    @Override
    public List<LenderResponseData> getAllByStatus(LenderStatus status) {
        return lenderRepositoryWrapper.findAllByStatus(status).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
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

