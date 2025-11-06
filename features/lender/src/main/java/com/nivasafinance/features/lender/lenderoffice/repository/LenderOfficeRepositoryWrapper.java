package com.nivasafinance.features.lender.lenderoffice.repository;

import com.nivasafinance.features.lender.lenderoffice.entity.LenderOffice;
import com.nivasafinance.features.lender.lenderoffice.enums.LenderOfficeStatus;
import com.nivasafinance.features.lender.lenderoffice.exception.LenderOfficeExceptionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LenderOfficeRepositoryWrapper {

    private final LenderOfficeRepository lenderOfficeRepository;
    private final MessageSource messageSource;

    @Autowired
    public LenderOfficeRepositoryWrapper(LenderOfficeRepository lenderOfficeRepository, MessageSource messageSource) {
        this.lenderOfficeRepository = lenderOfficeRepository;
        this.messageSource = messageSource;
    }

    public LenderOffice saveWithException(LenderOffice lenderOffice) {
        try {
            return lenderOfficeRepository.save(lenderOffice);
        } catch (Exception ex) {
            throw LenderOfficeExceptionFactory.createFailed(messageSource);
        }
    }

    public LenderOffice findByIdWithException(UUID id) {
        return lenderOfficeRepository.findById(id).orElseThrow(() ->
                LenderOfficeExceptionFactory.lenderOfficeNotFound(id, messageSource)
        );
    }

    public LenderOffice findByKeyWithException(String key) {
        return lenderOfficeRepository.findByKey(key).orElseThrow(() ->
                LenderOfficeExceptionFactory.lenderOfficeKeyNotFound(key, messageSource)
        );
    }

    public List<LenderOffice> findByLenderKeyAndStatus(String lenderKey, LenderOfficeStatus status) {
        return lenderOfficeRepository.findByLenderKeyAndStatus(lenderKey, status);
    }

    public void deleteByIdWithException(UUID id) {
        if (!lenderOfficeRepository.existsById(id)) {
            throw LenderOfficeExceptionFactory.lenderOfficeNotFound(id, messageSource);
        }
        lenderOfficeRepository.deleteById(id);
    }
}

