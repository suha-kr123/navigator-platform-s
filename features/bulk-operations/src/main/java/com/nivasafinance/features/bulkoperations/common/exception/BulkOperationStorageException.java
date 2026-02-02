package com.nivasafinance.features.bulkoperations.common.exception;

import java.io.Serial;

public class BulkOperationStorageException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String MESSAGE_NAME_REQUIRED = "Name is required";
    private static final String MESSAGE_CONTENT_REQUIRED = "Content is required";
    private static final String MESSAGE_TRANSFER_TO_NOT_SUPPORTED = "transferTo is not supported for stored files";

    public BulkOperationStorageException(String message) {
        super(message);
    }

    public BulkOperationStorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public static BulkOperationStorageException nameRequired() {
        return new BulkOperationStorageException(MESSAGE_NAME_REQUIRED);
    }

    public static BulkOperationStorageException contentRequired() {
        return new BulkOperationStorageException(MESSAGE_CONTENT_REQUIRED);
    }

    public static BulkOperationStorageException transferToNotSupported() {
        return new BulkOperationStorageException(MESSAGE_TRANSFER_TO_NOT_SUPPORTED);
    }
}
