package com.nivasafinance.features.advisor.repository;

import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.exception.AdvisorExceptionFactory;
import com.nivasafinance.features.advisor.exception.AdvisorNotFoundException;
import com.nivasafinance.features.advisor.exception.AdvisorOperationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AdvisorRepositoryWrapper {

    private final AdvisorRepository advisorRepository;
    private final MessageSource messageSource;

    @Autowired
    public AdvisorRepositoryWrapper(AdvisorRepository advisorRepository, MessageSource messageSource) {
        this.advisorRepository = advisorRepository;
        this.messageSource = messageSource;
    }

    public Advisor saveWithException(Advisor advisor) {
        try {
            return advisorRepository.save(advisor);
        } catch (DataAccessException e) {
            AdvisorOperationException exception = AdvisorExceptionFactory.createFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public Advisor findByIdWithException(UUID id) {
        try {
            return advisorRepository.findById(id).orElseThrow(() ->
                    AdvisorExceptionFactory.notFound(id, messageSource)
            );
        } catch (AdvisorNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            AdvisorOperationException exception = AdvisorExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public Page<Advisor> findAllWithException(Pageable pageable) {
        try {
            return advisorRepository.findAll(pageable);
        } catch (DataAccessException e) {
            AdvisorOperationException exception = AdvisorExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public Long countWithException() {
        try {
            return advisorRepository.count();
        } catch (DataAccessException e) {
            AdvisorOperationException exception = AdvisorExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public void deleteByIdWithException(UUID id) {
        try {
            advisorRepository.deleteById(id);
        } catch (DataAccessException e) {
            AdvisorOperationException exception = AdvisorExceptionFactory.deleteFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }
}

