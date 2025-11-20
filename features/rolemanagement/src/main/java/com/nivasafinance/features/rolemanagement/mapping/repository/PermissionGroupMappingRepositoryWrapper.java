package com.nivasafinance.features.rolemanagement.mapping.repository;

import com.nivasafinance.features.rolemanagement.exception.RoleManagementExceptionFactory;
import com.nivasafinance.features.rolemanagement.mapping.entity.PermissionGroupMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionGroupMappingRepositoryWrapper {
    
    private final PermissionGroupMappingRepository repository;
    private final MessageSource messageSource;
    
    public List<PermissionGroupMapping> findByPermissionGroupIdIn(List<Long> groupIds) {
        return repository.findByPermissionGroupIdIn(groupIds);
    }
    
    public PermissionGroupMapping findById(Long id) {
        return repository.findById(id).orElse(null);
    }
    
    public PermissionGroupMapping findByIdWithException(Long id) {
        PermissionGroupMapping mapping = findById(id);
        if (mapping == null) {
            throw RoleManagementExceptionFactory.notFound("permission.group.mapping", id, messageSource);
        }
        return mapping;
    }
}

