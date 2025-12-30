package com.nivasafinance.features.rolemanagement.permissionchecker.impl;

import com.nivasafinance.features.rolemanagement.enums.Role;
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
    public boolean checkPermissionForUser(String username, String permissionName) {
        List<String> roles = userRoleService.getRolesByUsername(username);
        if (roles.isEmpty()) {
            return false;
        }

        if(roles.contains(Role.ADMIN.name())){ // ADMIN will have all permissions
            return true;
        }
        
        List<PermissionResponse> permissions =
                permissionReadService.getPermissionsByRoles(roles);
        
        return permissions.stream()
                .anyMatch(p -> p.getName().equals(permissionName));
    }
}

