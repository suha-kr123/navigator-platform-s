package com.nivasafinance.features.rolemanagement.mapping.repository;

import com.nivasafinance.features.rolemanagement.exception.RoleManagementExceptionFactory;
import com.nivasafinance.features.rolemanagement.mapping.entity.RolePermissionGroupMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RolePermissionGroupMappingRepositoryWrapper {
    
    private final RolePermissionGroupMappingRepository repository;
    private final MessageSource messageSource;
    
    public List<RolePermissionGroupMapping> findByRoleIdIn(List<UUID> roleIds) {
        return repository.findByRoleIdIn(roleIds);
    }
    
    public RolePermissionGroupMapping findById(UUID id) {
        return repository.findById(id).orElse(null);
    }
    
    public RolePermissionGroupMapping findByIdWithException(UUID id) {
        RolePermissionGroupMapping mapping = findById(id);
        if (mapping == null) {
            throw RoleManagementExceptionFactory.notFound("role.permission.group.mapping", id, messageSource);
        }
        return mapping;
    }
}

