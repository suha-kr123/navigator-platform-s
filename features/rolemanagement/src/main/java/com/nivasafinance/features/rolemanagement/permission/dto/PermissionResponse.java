package com.nivasafinance.features.rolemanagement.permission.dto;

import com.nivasafinance.features.rolemanagement.enums.ActionEnum;
import com.nivasafinance.features.rolemanagement.enums.ModuleEnum;
import com.nivasafinance.features.rolemanagement.enums.OperationsEnum;
import com.nivasafinance.features.rolemanagement.permission.entity.Permission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionResponse {
    private Long id;
    private String name;
    private ActionEnum action;
    private OperationsEnum operation;
    private ModuleEnum module;
    
    public static PermissionResponse from(Permission permission) {
        if (permission == null || permission.getId() == null) {
            return null;
        }
        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .action(permission.getAction())
                .operation(permission.getOperation())
                .module(permission.getModule())
                .build();
    }
}

