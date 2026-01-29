package com.nivasafinance.features.rolemanagement.permission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionCheckResponse {
    private String permission;
    private Boolean hasPermission;
    private String username;

    public static PermissionCheckResponse of(String permission, Boolean hasPermission, String username) {
        return PermissionCheckResponse.builder()
                .permission(permission)
                .hasPermission(hasPermission)
                .username(username)
                .build();
    }
}
