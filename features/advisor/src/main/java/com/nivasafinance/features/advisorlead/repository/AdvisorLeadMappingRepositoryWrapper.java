package com.nivasafinance.features.advisorlead.repository;

import com.nivasafinance.features.advisorlead.entity.AdvisorLeadMapping;
import com.nivasafinance.features.advisorlead.exception.AdvisorLeadMappingExceptionFactory;
import com.nivasafinance.features.advisorlead.exception.AdvisorLeadMappingOperationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AdvisorLeadMappingRepositoryWrapper {

    private final AdvisorLeadMappingRepository advisorLeadMappingRepository;
    private final MessageSource messageSource;

    @Autowired
    public AdvisorLeadMappingRepositoryWrapper(
            AdvisorLeadMappingRepository advisorLeadMappingRepository,
            MessageSource messageSource) {
        this.advisorLeadMappingRepository = advisorLeadMappingRepository;
        this.messageSource = messageSource;
    }

    public AdvisorLeadMapping saveWithException(AdvisorLeadMapping advisorLeadMapping) {
        try {
            return advisorLeadMappingRepository.save(advisorLeadMapping);
        } catch (DataAccessException e) {
            AdvisorLeadMappingOperationException exception = AdvisorLeadMappingExceptionFactory.createFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public AdvisorLeadMapping findByIdWithException(UUID id) {
        try {
            return advisorLeadMappingRepository.findById(id).orElseThrow(() ->
                    AdvisorLeadMappingExceptionFactory.notFound(id, messageSource)
            );
        } catch (DataAccessException e) {
            AdvisorLeadMappingOperationException exception = AdvisorLeadMappingExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public Page<AdvisorLeadMapping> findAllByAdvisorIdWithException(UUID advisorId, Pageable pageable) {
        try {
            return advisorLeadMappingRepository.findAllByAdvisorId(advisorId, pageable);
        } catch (DataAccessException e) {
            AdvisorLeadMappingOperationException exception = AdvisorLeadMappingExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public void deleteByIdWithException(UUID id) {
        try {
            advisorLeadMappingRepository.deleteById(id);
        } catch (DataAccessException e) {
            AdvisorLeadMappingOperationException exception = AdvisorLeadMappingExceptionFactory.deleteFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }
}

