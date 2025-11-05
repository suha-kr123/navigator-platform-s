package com.nivasafinance.features.usermanagement.exception;

public class UserExceptionFactory {

    public static UserNotFoundException userNotFoundById(Long userId) {
        return new UserNotFoundException(String.format("User not found with id: %d", userId));
    }

    public static UserNotFoundException userNotFoundByUsername(String username) {
        return new UserNotFoundException(String.format("User not found with username: %s", username));
    }

    public static UserAlreadyExistsException userAlreadyExists(String username) {
        return new UserAlreadyExistsException(String.format("User already exists with username: %s", username));
    }

    public static UserOperationException userOperationFailed(String operation, Throwable cause) {
        return new UserOperationException(String.format("User operation failed: %s", operation), cause);
    }
}

