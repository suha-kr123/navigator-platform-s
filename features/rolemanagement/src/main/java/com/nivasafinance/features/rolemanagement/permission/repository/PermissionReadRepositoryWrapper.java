package com.nivasafinance.features.rolemanagement.permission.repository;

import com.nivasafinance.features.rolemanagement.mapping.repository.PermissionGroupMappingRepositoryWrapper;
import com.nivasafinance.features.rolemanagement.mapping.repository.RolePermissionGroupMappingRepositoryWrapper;
import com.nivasafinance.features.rolemanagement.mapping.repository.RolePermissionMappingRepositoryWrapper;
import com.nivasafinance.features.rolemanagement.permission.entity.Permission;
import com.nivasafinance.features.rolemanagement.role.repository.RoleRepositoryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionReadRepositoryWrapper {
    
    private final RoleRepositoryWrapper roleRepository;
    private final RolePermissionMappingRepositoryWrapper rolePermissionMappingRepository;
    private final RolePermissionGroupMappingRepositoryWrapper rolePermissionGroupMappingRepository;
    private final PermissionGroupMappingRepositoryWrapper permissionGroupMappingRepository;
    private final PermissionRepositoryWrapper permissionRepository;
    
    public List<Permission> findPermissionsByRoleNames(List<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<com.nivasafinance.features.rolemanagement.role.entity.Role> roles = roleRepository.findByNameIn(roleNames);
        if (roles.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<UUID> roleIds = roles.stream()
                .map(com.nivasafinance.features.rolemanagement.role.entity.Role::getId)
                .filter(id -> id != null)
                .collect(Collectors.toList());
        
        List<UUID> directPermissionIds = rolePermissionMappingRepository
                .findByRoleIdIn(roleIds)
                .stream()
                .map(com.nivasafinance.features.rolemanagement.mapping.entity.RolePermissionMapping::getPermissionId)
                .collect(Collectors.toList());
        
        List<UUID> groupIds = rolePermissionGroupMappingRepository
                .findByRoleIdIn(roleIds)
                .stream()
                .map(com.nivasafinance.features.rolemanagement.mapping.entity.RolePermissionGroupMapping::getPermissionGroupId)
                .collect(Collectors.toList());
        
        List<UUID> groupPermissionIds = new ArrayList<>();
        if (!groupIds.isEmpty()) {
            groupPermissionIds = permissionGroupMappingRepository
                    .findByPermissionGroupIdIn(groupIds)
                    .stream()
                    .map(com.nivasafinance.features.rolemanagement.mapping.entity.PermissionGroupMapping::getPermissionId)
                    .collect(Collectors.toList());
        }
        
        Set<UUID> allPermissionIds = new HashSet<>();
        allPermissionIds.addAll(directPermissionIds);
        allPermissionIds.addAll(groupPermissionIds);
        
        if (allPermissionIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        return permissionRepository.findByIdIn(new ArrayList<>(allPermissionIds));
    }
}

