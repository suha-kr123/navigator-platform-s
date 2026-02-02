package com.nivasafinance.features.bulkoperations.common.exception;

public class InvalidBulkOperationTypeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String operationType;

    public InvalidBulkOperationTypeException(String operationType) {
        super("Invalid bulk operation type: " + operationType);
        this.operationType = operationType;
    }

    public String getOperationType() {
        return operationType;
    }

    public static InvalidBulkOperationTypeException of(String operationType) {
        return new InvalidBulkOperationTypeException(operationType);
    }
}
