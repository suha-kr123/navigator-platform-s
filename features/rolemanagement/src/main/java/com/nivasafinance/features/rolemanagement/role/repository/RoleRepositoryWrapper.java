package com.nivasafinance.features.rolemanagement.role.repository;

import com.nivasafinance.features.rolemanagement.exception.RoleManagementExceptionFactory;
import com.nivasafinance.features.rolemanagement.role.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleRepositoryWrapper {
    
    private final RoleRepository roleRepository;
    private final MessageSource messageSource;
    
    public List<Role> findByNameIn(List<String> names) {
        return roleRepository.findByNameIn(names);
    }
    
    public Role findById(UUID id) {
        return roleRepository.findById(id).orElse(null);
    }
    
    public Role findByIdWithException(UUID id) {
        Role role = findById(id);
        if (role == null) {
            throw RoleManagementExceptionFactory.notFound("role", id, messageSource);
        }
        return role;
    }
}

