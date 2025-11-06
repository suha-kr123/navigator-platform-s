package com.nivasafinance.features.rolemanagement.permissiongroup.service.impl;

import com.nivasafinance.features.rolemanagement.mapping.repository.RolePermissionGroupMappingRepositoryWrapper;
import com.nivasafinance.features.rolemanagement.permissiongroup.dto.PermissionGroupResponse;
import com.nivasafinance.features.rolemanagement.permissiongroup.repository.PermissionGroupRepositoryWrapper;
import com.nivasafinance.features.rolemanagement.permissiongroup.service.PermissionGroupReadService;
import com.nivasafinance.features.rolemanagement.role.repository.RoleRepositoryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionGroupServiceImpl implements PermissionGroupReadService {
    
    private final PermissionGroupRepositoryWrapper permissionGroupRepositoryWrapper;
    private final RolePermissionGroupMappingRepositoryWrapper rolePermissionGroupMappingRepositoryWrapper;
    private final RoleRepositoryWrapper roleRepositoryWrapper;
    
    @Override
    public List<PermissionGroupResponse> getPermissionGroupsbyRoles(List<String> roleName) {
        if (roleName == null || roleName.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<com.nivasafinance.features.rolemanagement.role.entity.Role> roles = roleRepositoryWrapper.findByNameIn(roleName);
        if (roles.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<UUID> roleIds = roles.stream()
                .map(com.nivasafinance.features.rolemanagement.role.entity.Role::getId)
                .filter(id -> id != null)
                .collect(Collectors.toList());
        
        List<com.nivasafinance.features.rolemanagement.mapping.entity.RolePermissionGroupMapping> rolePermissionGroupMappings = 
                rolePermissionGroupMappingRepositoryWrapper.findByRoleIdIn(roleIds);
        
        List<UUID> permissionGroupIds = rolePermissionGroupMappings.stream()
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
                        .id(group.getId() != null ? group.getId() : UUID.randomUUID())
                        .name(group.getName())
                        .build())
                .collect(Collectors.toList());
    }
}

