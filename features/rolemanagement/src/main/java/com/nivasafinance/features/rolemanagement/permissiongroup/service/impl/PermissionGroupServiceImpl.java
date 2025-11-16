package com.nivasafinance.features.rolemanagement.permissiongroup.service.impl;

import com.nivasafinance.features.rolemanagement.mapping.repository.RolePermissionGroupMappingRepositoryWrapper;
import com.nivasafinance.features.rolemanagement.permissiongroup.dto.PermissionGroupResponse;
import com.nivasafinance.features.rolemanagement.permissiongroup.repository.PermissionGroupRepositoryWrapper;
import com.nivasafinance.features.rolemanagement.permissiongroup.service.PermissionGroupReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionGroupServiceImpl implements PermissionGroupReadService {
    
    private final PermissionGroupRepositoryWrapper permissionGroupRepositoryWrapper;
    private final RolePermissionGroupMappingRepositoryWrapper rolePermissionGroupMappingRepositoryWrapper;
    
    @Override
    public List<PermissionGroupResponse> getPermissionGroupsbyRoles(List<String> roleName) {
        if (roleName == null || roleName.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<com.nivasafinance.features.rolemanagement.mapping.entity.RolePermissionGroupMapping> rolePermissionGroupMappings = 
                rolePermissionGroupMappingRepositoryWrapper.findByRoleIn(roleName);
        
        List<Long> permissionGroupIds = rolePermissionGroupMappings.stream()
                .map(com.nivasafinance.features.rolemanagement.mapping.entity.RolePermissionGroupMapping::getPermissionGroupId)
                .distinct()
                .collect(Collectors.toList());
        
        if (permissionGroupIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<com.nivasafinance.features.rolemanagement.permissiongroup.entity.PermissionGroup> permissionGroups = 
                permissionGroupIds.stream()
                        .map(permissionGroupRepositoryWrapper::findById)
                        .filter(group -> group != null)
                        .collect(Collectors.toList());
        
        return permissionGroups.stream()
                .map(group -> PermissionGroupResponse.builder()
                        .id(group.getId())
                        .name(group.getName())
                        .build())
                .collect(Collectors.toList());
    }
}

