package com.nivasafinance.features.rolemanagement.mapping.repository;

import com.nivasafinance.features.rolemanagement.exception.RoleManagementExceptionFactory;
import com.nivasafinance.features.rolemanagement.mapping.entity.RolePermissionMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RolePermissionMappingRepositoryWrapper {
    
    private final RolePermissionMappingRepository repository;
    private final MessageSource messageSource;
    
    public List<RolePermissionMapping> findByRoleIn(List<String> roles) {
        return repository.findByRoleIn(roles);
    }
    
    public RolePermissionMapping findById(Long id) {
        return repository.findById(id).orElse(null);
    }
    
    public RolePermissionMapping findByIdWithException(Long id) {
        RolePermissionMapping mapping = findById(id);
        if (mapping == null) {
            throw RoleManagementExceptionFactory.notFound("role.permission.mapping", id, messageSource);
        }
        return mapping;
    }
}

