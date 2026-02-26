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
public class RolePermissionsUpdateResponse {
    private String role;
    private int addedCount;
    private int requestedCount;
    private List<Long> addedPermissionIds;
}
