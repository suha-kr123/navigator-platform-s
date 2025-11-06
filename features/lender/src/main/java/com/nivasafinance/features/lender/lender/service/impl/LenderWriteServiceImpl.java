package com.nivasafinance.features.lender.lender.service.impl;

import com.nivasafinance.features.lender.lender.dto.LenderRequestData;
import com.nivasafinance.features.lender.lender.dto.LenderResponseData;
import com.nivasafinance.features.lender.lender.entity.Lender;
import com.nivasafinance.features.lender.lender.repository.LenderRepositoryWrapper;
import com.nivasafinance.features.lender.lender.service.LenderWriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class LenderWriteServiceImpl implements LenderWriteService {

    private final LenderRepositoryWrapper lenderRepositoryWrapper;

    @Autowired
    public LenderWriteServiceImpl(LenderRepositoryWrapper lenderRepositoryWrapper) {
        this.lenderRepositoryWrapper = lenderRepositoryWrapper;
    }

    @Override
    public LenderResponseData create(LenderRequestData lenderData) {
        Lender lender = new Lender();
        lender.setKey(lenderData.getKey());
        lender.setName(lenderData.getName());
        lender.setStatus(lenderData.getStatus());
        
        Lender savedLender = lenderRepositoryWrapper.saveWithException(lender);
        return toResponse(savedLender);
    }

    @Override
    public LenderResponseData update(UUID id, LenderRequestData lenderData) {
        Lender existingLender = lenderRepositoryWrapper.findByIdWithException(id);

        existingLender.setKey(lenderData.getKey());
        existingLender.setName(lenderData.getName());
        existingLender.setStatus(lenderData.getStatus());
        
        Lender savedLender = lenderRepositoryWrapper.saveWithException(existingLender);
        return toResponse(savedLender);
    }

    @Override
    public void delete(UUID id) {
        lenderRepositoryWrapper.deleteByIdWithException(id);
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

