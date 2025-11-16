package com.nivasafinance.features.rolemanagement.permission.repository;

import com.nivasafinance.features.rolemanagement.mapping.repository.PermissionGroupMappingRepositoryWrapper;
import com.nivasafinance.features.rolemanagement.mapping.repository.RolePermissionGroupMappingRepositoryWrapper;
import com.nivasafinance.features.rolemanagement.mapping.repository.RolePermissionMappingRepositoryWrapper;
import com.nivasafinance.features.rolemanagement.permission.entity.Permission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionReadRepositoryWrapper {
    
    private final RolePermissionMappingRepositoryWrapper rolePermissionMappingRepository;
    private final RolePermissionGroupMappingRepositoryWrapper rolePermissionGroupMappingRepository;
    private final PermissionGroupMappingRepositoryWrapper permissionGroupMappingRepository;
    private final PermissionRepositoryWrapper permissionRepository;
    
    public List<Permission> findPermissionsByRoleNames(List<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Long> directPermissionIds = rolePermissionMappingRepository
                .findByRoleIn(roleNames)
                .stream()
                .map(com.nivasafinance.features.rolemanagement.mapping.entity.RolePermissionMapping::getPermissionId)
                .collect(Collectors.toList());
        
        List<Long> groupIds = rolePermissionGroupMappingRepository
                .findByRoleIn(roleNames)
                .stream()
                .map(com.nivasafinance.features.rolemanagement.mapping.entity.RolePermissionGroupMapping::getPermissionGroupId)
                .collect(Collectors.toList());
        
        List<Long> groupPermissionIds = new ArrayList<>();
        if (!groupIds.isEmpty()) {
            groupPermissionIds = permissionGroupMappingRepository
                    .findByPermissionGroupIdIn(groupIds)
                    .stream()
                    .map(com.nivasafinance.features.rolemanagement.mapping.entity.PermissionGroupMapping::getPermissionId)
                    .collect(Collectors.toList());
        }
        
        Set<Long> allPermissionIds = new HashSet<>();
        allPermissionIds.addAll(directPermissionIds);
        allPermissionIds.addAll(groupPermissionIds);
        
        if (allPermissionIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        return permissionRepository.findByIdIn(new ArrayList<>(allPermissionIds));
    }
}

