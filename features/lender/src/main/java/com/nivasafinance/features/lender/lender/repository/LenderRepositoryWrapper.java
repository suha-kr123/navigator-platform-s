package com.nivasafinance.features.lender.lender.repository;

import com.nivasafinance.features.lender.lender.entity.Lender;
import com.nivasafinance.features.lender.lender.enums.LenderStatus;
import com.nivasafinance.features.lender.lender.exception.LenderExceptionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LenderRepositoryWrapper {

    private final LenderRepository lenderRepository;
    private final MessageSource messageSource;

    @Autowired
    public LenderRepositoryWrapper(LenderRepository lenderRepository, MessageSource messageSource) {
        this.lenderRepository = lenderRepository;
        this.messageSource = messageSource;
    }

    public Lender saveWithException(Lender lender) {
        try {
            return lenderRepository.save(lender);
        } catch (Exception e) {
            throw LenderExceptionFactory.createFailed(messageSource);
        }
    }

    public Lender findByIdWithException(UUID id) {
        return lenderRepository.findById(id).orElseThrow(() ->
                LenderExceptionFactory.lenderNotFound(id, messageSource)
        );
    }

    public Lender findByKeyWithException(String key) {
        return lenderRepository.findByKey(key).orElseThrow(() ->
                LenderExceptionFactory.lenderNotFound(key, messageSource)
        );
    }

    public List<Lender> findAll() {
        return lenderRepository.findAll();
    }

    public List<Lender> findAllByStatus(LenderStatus status) {
        return lenderRepository.findByStatus(status);
    }

    public void deleteByIdWithException(UUID id) {
        if (!lenderRepository.existsById(id)) {
            throw LenderExceptionFactory.lenderNotFound(id, messageSource);
        }
        lenderRepository.deleteById(id);
    }
}

