package com.nivasafinance.features.rolemanagement.role.service;

import com.nivasafinance.features.rolemanagement.mapping.repository.UserRoleMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserRoleService {
    
    private final UserRoleMappingRepository userRoleMappingRepository;
    
    public List<String> getRolesByUsername(String username) {
        return userRoleMappingRepository.findByUsername(username)
                .map(mapping -> List.of(mapping.getRole()))
                .orElse(Collections.emptyList());
    }
}

