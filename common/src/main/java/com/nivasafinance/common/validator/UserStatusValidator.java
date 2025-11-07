package com.nivasafinance.common.validator;

import com.nivasafinance.common.exception.UnauthorizedException;

/**
 * Validates that a user exists in the system and is active.
 */
public interface UserStatusValidator {

    /**
     * Validate that the provided username belongs to an active user.
     *
     * @param username the username extracted from the request header
     * @throws UnauthorizedException
     *         if the user does not exist or is not active
     */
    void validateActiveUser(String username);
}

