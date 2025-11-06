package com.nivasafinance.features.rolemanagement.permissiongroup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionGroupResponse {
    private UUID id;
    private String name;
}

