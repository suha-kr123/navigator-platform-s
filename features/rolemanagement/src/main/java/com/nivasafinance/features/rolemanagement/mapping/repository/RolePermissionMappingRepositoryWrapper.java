package com.nivasafinance.features.rolemanagement.mapping.repository;

import com.nivasafinance.features.rolemanagement.exception.RoleManagementExceptionFactory;
import com.nivasafinance.features.rolemanagement.mapping.entity.RolePermissionMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RolePermissionMappingRepositoryWrapper {
    
    private final RolePermissionMappingRepository repository;
    private final MessageSource messageSource;
    
    public List<RolePermissionMapping> findByRoleIdIn(List<UUID> roleIds) {
        return repository.findByRoleIdIn(roleIds);
    }
    
    public RolePermissionMapping findById(UUID id) {
        return repository.findById(id).orElse(null);
    }
    
    public RolePermissionMapping findByIdWithException(UUID id) {
        RolePermissionMapping mapping = findById(id);
        if (mapping == null) {
            throw RoleManagementExceptionFactory.notFound("role.permission.mapping", id, messageSource);
        }
        return mapping;
    }
}

