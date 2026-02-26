package com.nivasafinance.features.rolemanagement.permissiongroup.repository;

import com.nivasafinance.features.rolemanagement.exception.RoleManagementExceptionFactory;
import com.nivasafinance.features.rolemanagement.permissiongroup.entity.PermissionGroup;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionGroupRepositoryWrapper {
    
    private final PermissionGroupRepository repository;
    private final MessageSource messageSource;
    
    public List<PermissionGroup> findAll() {
        return repository.findAll();
    }
    
    public Page<PermissionGroup> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }
    
    public Page<PermissionGroup> findByNameContainingIgnoreCase(String name, Pageable pageable) {
        return repository.findByNameContainingIgnoreCase(name, pageable);
    }
    
    public PermissionGroup findById(Long id) {
        return repository.findById(id).orElse(null);
    }
    
    public PermissionGroup findByIdWithException(Long id) {
        PermissionGroup group = findById(id);
        if (group == null) {
            throw RoleManagementExceptionFactory.notFound("permission.group", id, messageSource);
        }
        return group;
    }
}
