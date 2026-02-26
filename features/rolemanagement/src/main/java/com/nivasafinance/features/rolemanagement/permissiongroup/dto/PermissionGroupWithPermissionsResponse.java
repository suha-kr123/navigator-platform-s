package com.nivasafinance.features.rolemanagement.permissiongroup.dto;

import com.nivasafinance.features.rolemanagement.permission.dto.PermissionResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionGroupWithPermissionsResponse {
    private Long id;
    private String name;
    private List<PermissionResponse> permissions;
}
