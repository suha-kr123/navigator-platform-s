package com.nivasafinance.features.lead.repository;

import com.nivasafinance.features.lead.entity.Lead;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LeadRepositoryWrapper {

    private final LeadRepository leadRepository;
    private final MessageSource messageSource;
    private final JdbcTemplate jdbcTemplate;

    public Lead saveWithException(Lead lead) {
        try {
            return leadRepository.save(lead);
        } catch (DataAccessException e) {
            RuntimeException exception = new RuntimeException("Failed to save lead", e);
            exception.initCause(e);
            throw exception;
        }
    }

    public Lead findByIdWithException(Long id) {
        try {
            return leadRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Lead not found with id: " + id));
        } catch (DataAccessException e) {
            RuntimeException exception = new RuntimeException("Failed to retrieve lead", e);
            exception.initCause(e);
            throw exception;
        }
    }
}
