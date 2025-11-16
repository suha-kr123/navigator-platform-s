package com.nivasafinance.features.rolemanagement.permissiongroup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionGroupResponse {
    private Long id;
    private String name;
}

