package com.nivasafinance.features.rolemanagement.permissiongroup.repository;

import com.nivasafinance.features.rolemanagement.exception.RoleManagementExceptionFactory;
import com.nivasafinance.features.rolemanagement.permissiongroup.entity.PermissionGroup;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PermissionGroupRepositoryWrapper {
    
    private final PermissionGroupRepository repository;
    private final MessageSource messageSource;
    
    public List<PermissionGroup> findAll() {
        return repository.findAll();
    }
    
    public PermissionGroup findById(UUID id) {
        return repository.findById(id).orElse(null);
    }
    
    public PermissionGroup findByIdWithException(UUID id) {
        PermissionGroup group = findById(id);
        if (group == null) {
            throw RoleManagementExceptionFactory.notFound("permission.group", id, messageSource);
        }
        return group;
    }
}

