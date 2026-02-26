package com.nivasafinance.features.rolemanagement.permission.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateRolePermissionsRequest {
    private List<Long> permissionIds;
}
