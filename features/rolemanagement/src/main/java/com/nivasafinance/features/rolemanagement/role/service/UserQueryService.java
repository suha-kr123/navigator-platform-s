package com.nivasafinance.features.rolemanagement.role.service;

import com.nivasafinance.features.rolemanagement.role.dto.UserAssignmentResponse;

import java.util.List;

public interface UserQueryService {
    /**
     * Get users by roles based on provided office hierarchy (includes current office, children, and parents).
     * @param roles List of role names
     * @param officeKey Office key to determine hierarchy
     * @return List of assignable users
     */
    List<UserAssignmentResponse> getUsersByOfficeAndRoles(List<String> roles, String officeKey);
    
    /**
     * Get users by roles based on provided office hierarchy (only current office and children, no parents).
     * @param roles List of role names
     * @param officeKey Office key to determine hierarchy
     * @return List of assignable users
     */
    List<UserAssignmentResponse> getUsersByOfficeAndRolesDownHierarchy(List<String> roles, String officeKey);
}

