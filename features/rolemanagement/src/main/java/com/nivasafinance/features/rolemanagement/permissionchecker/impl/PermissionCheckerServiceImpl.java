package com.nivasafinance.features.rolemanagement.permissionchecker.impl;

import com.nivasafinance.features.rolemanagement.enums.ActionEnum;
import com.nivasafinance.features.rolemanagement.enums.ModuleEnum;
import com.nivasafinance.features.rolemanagement.enums.OperationsEnum;
import com.nivasafinance.features.rolemanagement.permission.dto.PermissionResponse;
import com.nivasafinance.features.rolemanagement.permission.service.PermissionReadService;
import com.nivasafinance.features.rolemanagement.permissionchecker.PermissionCheckerService;
import com.nivasafinance.features.rolemanagement.role.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionCheckerServiceImpl implements PermissionCheckerService {
    
    private final PermissionReadService permissionReadService;
    private final UserRoleService userRoleService;
    
    @Override
    public boolean checkPermissionForUser(String username, ActionEnum action, ModuleEnum module, OperationsEnum operation) {
        List<String> roles = userRoleService.getRolesByUsername(username);
        if (roles.isEmpty()) {
            return false;
        }
        
        List<PermissionResponse> permissions =
                permissionReadService.getPermissionsByRoles(roles);
        
        return permissions.stream()
                .anyMatch(p -> p.getAction() == action && p.getModule() == module);
    }
}

