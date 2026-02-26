package com.nivasafinance.features.rolemanagement.permissiongroup.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateRolePermissionGroupsRequest {
    private List<Long> permissionGroupIds;
}
