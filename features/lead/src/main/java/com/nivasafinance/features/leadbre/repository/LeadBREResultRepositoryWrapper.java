package com.nivasafinance.features.leadbre.repository;

import com.nivasafinance.features.leadbre.entity.LeadBREResult;
import com.nivasafinance.features.leadbre.exception.LeadBREResultExceptionFactory;
import com.nivasafinance.features.leadbre.exception.LeadBREResultNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeadBREResultRepositoryWrapper {

    private final LeadBREResultRepository leadBREResultRepository;
    private final MessageSource messageSource;

    public LeadBREResult saveWithException(LeadBREResult entity) {
        try {
            return leadBREResultRepository.save(entity);
        } catch (DataAccessException e) {
            throw LeadBREResultExceptionFactory.saveFailed(messageSource);
        }
    }

    public List<LeadBREResult> findByLeadIdWithException(Long leadId) {
        try {
            return leadBREResultRepository.findByLeadIdOrderByCreatedAtDesc(leadId);
        } catch (DataAccessException e) {
            throw LeadBREResultExceptionFactory.retrieveFailed(messageSource);
        }
    }

    public List<LeadBREResult> findByLeadIdAndConfigNameWithException(Long leadId, String configName) {
        try {
            return leadBREResultRepository.findByLeadIdAndConfigNameOrderByCreatedAtDesc(leadId, configName);
        } catch (DataAccessException e) {
            throw LeadBREResultExceptionFactory.retrieveFailed(messageSource);
        }
    }

    public Optional<LeadBREResult> findLatestByLeadIdAndConfigName(Long leadId, String configName) {
        try {
            return leadBREResultRepository.findTopByLeadIdAndConfigNameOrderByCreatedAtDesc(leadId, configName);
        } catch (DataAccessException e) {
            throw LeadBREResultExceptionFactory.retrieveFailed(messageSource);
        }
    }

    public LeadBREResult findByLeadIdAndIdentifierWithException(Long leadId, UUID identifier) {
        try {
            LeadBREResult result = leadBREResultRepository.findByIdentifier(identifier)
                    .orElseThrow(() -> LeadBREResultExceptionFactory.notFound(identifier, messageSource));
            if (!leadId.equals(result.getLeadId())) {
                throw LeadBREResultExceptionFactory.notFound(identifier, messageSource);
            }
            return result;
        } catch (LeadBREResultNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            throw LeadBREResultExceptionFactory.retrieveFailed(messageSource);
        }
    }
}
