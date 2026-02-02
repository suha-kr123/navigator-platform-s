package com.nivasafinance.features.rolemanagement.permission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionBatchCheckResponse {
    private Map<String, Boolean> permissions;
    private String username;

    public static PermissionBatchCheckResponse of(Map<String, Boolean> permissions, String username) {
        return PermissionBatchCheckResponse.builder()
                .permissions(permissions)
                .username(username)
                .build();
    }
}
