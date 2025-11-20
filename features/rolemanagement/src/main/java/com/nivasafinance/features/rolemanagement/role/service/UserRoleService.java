package com.nivasafinance.features.rolemanagement.role.service;

import com.nivasafinance.features.rolemanagement.mapping.entity.UserRoleMapping;
import com.nivasafinance.features.rolemanagement.mapping.repository.UserRoleMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Nullable
    public String getPrimaryRoleForUsername(String username) {
        return userRoleMappingRepository.findByUsernameAndIsPrimary(username, true)
                .map(UserRoleMapping::getRole)
                .orElse(null);
    }

    @Transactional
    public void saveRolesForUsername(String username, String roleName, Boolean isPrimary) {
        UserRoleMapping userRoleMapping = new UserRoleMapping();
        userRoleMapping.setUsername(username);
        userRoleMapping.setRole(roleName);
        userRoleMapping.setIsPrimary(isPrimary);
        userRoleMappingRepository.save(userRoleMapping);
    }
}

