package com.nivasafinance.features.lead.repository;

import com.nivasafinance.features.lead.entity.Applicant;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class ApplicantRepositoryWrapper {

    private final ApplicantRepository applicantRepository;
    public ApplicantRepositoryWrapper(ApplicantRepository applicantRepository, MessageSource messageSource) {
        this.applicantRepository = applicantRepository;
    }

    public Applicant saveWithException(Applicant applicant) {
        try {
            return applicantRepository.save(applicant);
        } catch (DataAccessException e) {
            RuntimeException exception = new RuntimeException("Failed to save applicant", e);
            exception.initCause(e);
            throw exception;
        }
    }

    public Applicant findByIdWithException(Long id) {
        try {
            return applicantRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Applicant not found with id: " + id));
        } catch (DataAccessException e) {
            RuntimeException exception = new RuntimeException("Failed to retrieve applicant", e);
            exception.initCause(e);
            throw exception;
        }
    }

    public void delete(Applicant applicant) {
        try {
            applicantRepository.delete(applicant);
        } catch (DataAccessException e) {
            RuntimeException exception = new RuntimeException("Failed to delete applicant", e);
            exception.initCause(e);
            throw exception;
        }
    }
}
