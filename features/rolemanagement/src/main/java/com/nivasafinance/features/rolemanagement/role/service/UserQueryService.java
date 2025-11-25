package com.nivasafinance.features.rolemanagement.role.service;

import com.nivasafinance.features.rolemanagement.role.dto.UserAssignmentResponse;

import java.util.List;

public interface UserQueryService {
    List<UserAssignmentResponse> getUsersByOfficeAndRoles(List<String> roles);
}

