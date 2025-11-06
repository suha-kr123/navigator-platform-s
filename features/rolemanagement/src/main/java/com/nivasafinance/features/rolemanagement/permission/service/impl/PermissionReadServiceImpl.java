package com.nivasafinance.features.rolemanagement.permission.service.impl;

import com.nivasafinance.features.rolemanagement.permission.dto.PermissionResponse;
import com.nivasafinance.features.rolemanagement.permission.repository.PermissionReadRepositoryWrapper;
import com.nivasafinance.features.rolemanagement.permission.service.PermissionReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionReadServiceImpl implements PermissionReadService {
    
    private final PermissionReadRepositoryWrapper permissionReadRepositoryWrapper;
    
    @Override
    public List<PermissionResponse> getPermissionsByRoles(List<String> roleName) {
        if (roleName == null || roleName.isEmpty()) {
            return new ArrayList<>();
        }
        List<com.nivasafinance.features.rolemanagement.permission.entity.Permission> permissions = 
                permissionReadRepositoryWrapper.findPermissionsByRoleNames(roleName);
        return permissions.stream()
                .map(PermissionResponse::from)
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }
}

