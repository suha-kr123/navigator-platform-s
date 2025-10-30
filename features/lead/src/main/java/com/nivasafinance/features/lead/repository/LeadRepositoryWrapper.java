package com.nivasafinance.features.lead.repository;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
public class LeadRepositoryWrapper {

    private final LeadRepository leadRepository;
    private final MessageSource messageSource;

    public LeadRepositoryWrapper(LeadRepository leadRepository, MessageSource messageSource) {
        this.leadRepository = leadRepository;
        this.messageSource = messageSource;
    }
}
