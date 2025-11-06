package com.nivasafinance.features.rolemanagement.permission.repository;

import com.nivasafinance.features.rolemanagement.exception.RoleManagementExceptionFactory;
import com.nivasafinance.features.rolemanagement.permission.entity.Permission;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PermissionRepositoryWrapper {
    
    private final PermissionRepository permissionRepository;
    private final MessageSource messageSource;
    
    public List<Permission> findByIdIn(List<UUID> ids) {
        return permissionRepository.findByIdIn(ids);
    }
    
    public Permission findById(UUID id) {
        return permissionRepository.findById(id).orElse(null);
    }
    
    public Permission findByIdWithException(UUID id) {
        Permission permission = findById(id);
        if (permission == null) {
            throw RoleManagementExceptionFactory.notFound("permission", id, messageSource);
        }
        return permission;
    }
}

