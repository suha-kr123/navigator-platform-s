package com.nivasafinance.features.rolemanagement.exception;

import com.nivasafinance.common.exception.UnauthorizedException;

import java.io.Serial;

public class RoleManagementUnauthorizedException extends UnauthorizedException {

    @Serial
    private static final long serialVersionUID = 1L;

    public RoleManagementUnauthorizedException(String message) {
        super(message);
    }

}
