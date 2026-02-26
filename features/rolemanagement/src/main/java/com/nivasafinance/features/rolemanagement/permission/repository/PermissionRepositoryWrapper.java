package com.nivasafinance.features.rolemanagement.permission.repository;

import com.nivasafinance.features.rolemanagement.exception.RoleManagementExceptionFactory;
import com.nivasafinance.features.rolemanagement.permission.entity.Permission;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class PermissionRepositoryWrapper {
    
    private final PermissionRepository permissionRepository;
    private final MessageSource messageSource;
    
    public List<Permission> findAll() {
        return permissionRepository.findAll().stream().filter(Objects::nonNull).collect(Collectors.toList());
    }
    
    public List<Permission> findByIdIn(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return permissionRepository.findByIdIn(ids);
    }
    
    public Permission findById(Long id) {
        if (id == null) {
            return null;
        }
        return permissionRepository.findById(id).orElse(null);
    }
    
    public Permission findByIdWithException(Long id) {
        Permission permission = findById(id);
        if (permission == null) {
            throw RoleManagementExceptionFactory.notFound("permission", id, messageSource);
        }
        return permission;
    }
    
    public List<Permission> findByIdInOrThrow(List<Long> ids) {
        List<Permission> permissions = findByIdIn(ids);
        if (permissions.size() != (ids == null ? 0 : ids.size())) {
            Set<Long> foundIds = permissions.stream()
                    .map(Permission::getId)
                    .collect(Collectors.toSet());   
            for (Long id : ids) {
                if (!foundIds.contains(id)) {
                    throw RoleManagementExceptionFactory.notFound("permission", id, messageSource);
                }
            }
        }
        return permissions;
    }
    
    public Page<Permission> findAll(Pageable pageable) {
        return permissionRepository.findAll(pageable);
    }
    
    public Page<Permission> findByNameContainingIgnoreCase(String name, Pageable pageable) {
        return permissionRepository.findByNameContainingIgnoreCase(name, pageable);
    }
}
