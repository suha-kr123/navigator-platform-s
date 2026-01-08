package com.nivasafinance.features.rolemanagement.permissionchecker;

public interface PermissionCheckerService {
    boolean checkPermissionForUser(
            String username,
            String permissionName
    );
}

