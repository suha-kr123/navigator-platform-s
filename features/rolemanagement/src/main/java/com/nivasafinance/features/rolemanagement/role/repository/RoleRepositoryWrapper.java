package com.nivasafinance.features.rolemanagement.role.repository;

import com.nivasafinance.features.rolemanagement.exception.RoleManagementExceptionFactory;
import com.nivasafinance.features.rolemanagement.role.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleRepositoryWrapper {
    
    private final RoleRepository roleRepository;
    private final MessageSource messageSource;
    
    public List<Role> findAll() {
        return roleRepository.findAll();
    }
    
    public Page<Role> findAll(Pageable pageable) {
        return roleRepository.findAll(pageable);
    }
    
    public Page<Role> findByNameContainingIgnoreCase(String name, Pageable pageable) {
        return roleRepository.findByNameContainingIgnoreCase(name, pageable);
    }
    
    public java.util.Optional<Role> findByNameIgnoreCase(String name) {
        return roleRepository.findByNameIgnoreCase(name);
    }
    
    public Role save(Role role) {
        return roleRepository.save(role);
    }
    
    public List<Role> findByNameIn(List<String> names) {
        return roleRepository.findByNameIn(names);
    }
    
    public Role findById(Long id) {
        return roleRepository.findById(id).orElse(null);
    }
    
    public Role findByIdWithException(Long id) {
        Role role = findById(id);
        if (role == null) {
            throw RoleManagementExceptionFactory.notFound("role", id, messageSource);
        }
        return role;
    }
}
