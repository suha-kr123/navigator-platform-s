package com.nivasafinance.features.leadlender.repository;

import com.nivasafinance.features.leadlender.entity.LeadLender;
import com.nivasafinance.features.leadlender.enums.LeadLenderStatus;
import com.nivasafinance.features.leadlender.exception.LeadLenderExceptionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeadLenderRepositoryWrapper {

    private final LeadLenderRepository leadLenderRepository;
    private final MessageSource messageSource;

    public LeadLender saveWithException(LeadLender leadLender) {
        try {
            return leadLenderRepository.save(leadLender);
        } catch (Exception ex) {
            throw LeadLenderExceptionFactory.createFailed(messageSource);
        }
    }

    public LeadLender findByIdWithException(Long id) {
        return leadLenderRepository.findById(id)
            .orElseThrow(() -> LeadLenderExceptionFactory.leadLenderNotFound(messageSource));
    }

    public LeadLender findByLenderIdentifierWithException(UUID lenderIdentifier) {
        return leadLenderRepository.findByLenderIdentifier(lenderIdentifier)
            .orElseThrow(() -> LeadLenderExceptionFactory.leadLenderNotFound(lenderIdentifier, messageSource));
    }

    public List<LeadLender> findByLeadId(UUID leadId) {
        return leadLenderRepository.findByLeadId(leadId);
    }

    public List<LeadLender> findByLenderKey(String lenderKey) {
        return leadLenderRepository.findByLenderKey(lenderKey);
    }

    public List<LeadLender> findByStatus(LeadLenderStatus status) {
        return leadLenderRepository.findByStatus(status);
    }

    public List<LeadLender> findByLeadIdAndStatus(UUID leadId, LeadLenderStatus status) {
        return leadLenderRepository.findByLeadIdAndStatus(leadId, status);
    }

    public List<LeadLender> findByLeadIdAndStatusOrderByCreatedAtDesc(UUID leadId, 
                                                                       LeadLenderStatus status) {
        return leadLenderRepository.findByLeadIdAndStatusOrderByCreatedAtDesc(leadId, status);
    }

    public LeadLender findByLeadIdAndLenderKey(UUID leadId, String lenderKey) {
        return leadLenderRepository.findByLeadIdAndLenderKey(leadId, lenderKey)
            .orElse(null);
    }

    public LeadLender findByLeadIdAndLenderKeyWithException(UUID leadId, String lenderKey) {
        LeadLender leadLender = findByLeadIdAndLenderKey(leadId, lenderKey);
        if (leadLender == null) {
            throw LeadLenderExceptionFactory.leadLenderNotFound(leadId, lenderKey, messageSource);
        }
        return leadLender;
    }

    public void deleteByLenderIdentifierWithException(UUID lenderIdentifier) {
        LeadLender leadLender = findByLenderIdentifierWithException(lenderIdentifier);
        leadLenderRepository.delete(leadLender);
    }
}

