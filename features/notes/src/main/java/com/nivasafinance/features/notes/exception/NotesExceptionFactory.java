package com.nivasafinance.features.notes.exception;

import org.springframework.context.MessageSource;
import java.util.UUID;

public class NotesExceptionFactory {

    private NotesExceptionFactory() {
        // Private constructor to prevent instantiation
    }

    public static NotesNotFoundException notFound(UUID notesId, MessageSource messageSource) {
        return new NotesNotFoundException(notesId, messageSource);
    }

    public static NotesNotFoundException notFound(Long notesId, MessageSource messageSource) {
        return new NotesNotFoundException(notesId, messageSource);
    }

    public static NotesOperationException createFailed(MessageSource messageSource) {
        return new NotesOperationException("error.notes.operation.create", messageSource);
    }

    public static NotesOperationException deleteFailed(MessageSource messageSource) {
        return new NotesOperationException("error.notes.operation.delete", messageSource);
    }

    public static NotesOperationException retrieveEntityFailed(MessageSource messageSource) {
        return new NotesOperationException("error.notes.operation.retrieve", messageSource);
    }
}

