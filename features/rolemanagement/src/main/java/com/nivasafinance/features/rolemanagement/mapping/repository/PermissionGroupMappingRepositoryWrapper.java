package com.nivasafinance.features.rolemanagement.mapping.repository;

import com.nivasafinance.features.rolemanagement.exception.RoleManagementExceptionFactory;
import com.nivasafinance.features.rolemanagement.mapping.entity.PermissionGroupMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PermissionGroupMappingRepositoryWrapper {
    
    private final PermissionGroupMappingRepository repository;
    private final MessageSource messageSource;
    
    public List<PermissionGroupMapping> findByPermissionGroupIdIn(List<UUID> groupIds) {
        return repository.findByPermissionGroupIdIn(groupIds);
    }
    
    public PermissionGroupMapping findById(UUID id) {
        return repository.findById(id).orElse(null);
    }
    
    public PermissionGroupMapping findByIdWithException(UUID id) {
        PermissionGroupMapping mapping = findById(id);
        if (mapping == null) {
            throw RoleManagementExceptionFactory.notFound("permission.group.mapping", id, messageSource);
        }
        return mapping;
    }
}

