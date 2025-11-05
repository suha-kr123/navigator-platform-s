package com.nivasafinance.features.lead.repository;

import com.nivasafinance.features.lead.entity.Contact;
import com.nivasafinance.features.lead.exception.LeadExceptionFactory;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ContactRepositoryWrapper {

    private final ContactRepository contactRepository;
    private final MessageSource messageSource;

    public ContactRepositoryWrapper(ContactRepository contactRepository, MessageSource messageSource) {
        this.contactRepository = contactRepository;
        this.messageSource = messageSource;
    }

    public Contact saveWithException(Contact contact) {
        try {
            return contactRepository.save(contact);
        } catch (DataAccessException e) {
            RuntimeException exception = new RuntimeException("Failed to save contact", e);
            exception.initCause(e);
            throw exception;
        }
    }

    public Contact findByIdWithException(Long id) {
        try {
            return contactRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Contact not found with id: " + id));
        } catch (DataAccessException e) {
            RuntimeException exception = new RuntimeException("Failed to retrieve contact", e);
            exception.initCause(e);
            throw exception;
        }
    }

    public Contact findByIdentifierWithException(UUID identifier) {
        try {
            return contactRepository.findByIdentifier(identifier).orElseThrow(() ->
                    new RuntimeException("Contact not found with id: " + identifier)
            );
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to retrieve contact by identifier", e);
        }
    }
}
