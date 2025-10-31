package com.nivasafinance.features.notes.repository;

import com.nivasafinance.features.notes.entity.Notes;
import com.nivasafinance.features.notes.exception.NotesExceptionFactory;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public void deleteByIdWithException(UUID notesId) {
        try {
            notesRepository.deleteById(notesId);
        } catch (RuntimeException e) {
            throw NotesExceptionFactory.deleteFailed(messageSource);
        }
    }

    public Notes findByIdWithException(UUID notesId) {
        try {
            return notesRepository.findById(notesId).orElseThrow(() ->
                    NotesExceptionFactory.notFound(notesId, messageSource)
            );
        } catch (Exception e) {
            throw NotesExceptionFactory.notFound(notesId, messageSource);
        }
    }

    public Page<Notes> findAll(Pageable pageable) {
        try {
            return notesRepository.findAll(pageable);
        } catch (Exception e) {
            throw NotesExceptionFactory.retrieveEntityFailed(messageSource);
        }
    }
}

