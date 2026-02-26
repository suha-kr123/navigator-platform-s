package com.nivasafinance.features.rolemanagement.role.dto;

import lombok.Data;

import java.util.List;

@Data
public class AddUserRolesRequest {
    private List<String> roles;
    private String primaryRole;
}
