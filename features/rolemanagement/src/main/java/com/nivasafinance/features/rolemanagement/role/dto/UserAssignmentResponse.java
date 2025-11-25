package com.nivasafinance.features.rolemanagement.role.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAssignmentResponse {
    private String username;
    private String name;
    private String officeKey;
    private String officeName;
    private List<String> roles;
}

