package com.nivasafinance.features.bulkoperations.common.exception;

import com.nivasafinance.common.exception.ResourceConflictException;

/**
 * Thrown when the same file (by content hash) was already uploaded by the same user within the duplicate window.
 * Extends ResourceConflictException so global handler returns HTTP 409 Conflict.
 */
public class BulkOperationDuplicateUploadException extends ResourceConflictException {

    private static final long serialVersionUID = 1L;

    public BulkOperationDuplicateUploadException(String message) {
        super(message);
    }
}
