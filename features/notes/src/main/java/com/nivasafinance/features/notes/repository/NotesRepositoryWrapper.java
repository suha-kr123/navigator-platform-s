package com.nivasafinance.features.notes.repository;

import com.nivasafinance.features.notes.entity.Notes;
import com.nivasafinance.features.notes.exception.NotesExceptionFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class NotesRepositoryWrapper {

    private final NotesRepository notesRepository;
    private final MessageSource messageSource;

    public NotesRepositoryWrapper(NotesRepository notesRepository, MessageSource messageSource) {
        this.notesRepository = notesRepository;
        this.messageSource = messageSource;
    }

    public Notes saveWithException(Notes notes) {
        try {
            return notesRepository.save(notes);
        } catch (RuntimeException e) {
            throw NotesExceptionFactory.createFailed(messageSource);
        }
    }

    public void deleteByIdWithException(Long notesId) {
        try {
            notesRepository.deleteById(notesId);
        } catch (RuntimeException e) {
            throw NotesExceptionFactory.deleteFailed(messageSource);
        }
    }

    public Notes findByIdWithException(Long notesId) {
        try {
            return notesRepository.findById(notesId).orElseThrow(() ->
                    NotesExceptionFactory.notFound(notesId, messageSource)
            );
        } catch (Exception e) {
            throw NotesExceptionFactory.notFound(notesId, messageSource);
        }
    }

    public Notes findByIdentifierWithException(UUID identifier) {
        try {
            return notesRepository.findByIdentifier(identifier).orElseThrow(() ->
                    NotesExceptionFactory.notFound(identifier, messageSource)
            );
        } catch (Exception e) {
            throw NotesExceptionFactory.notFound(identifier, messageSource);
        }
    }
}

