package com.nivasafinance.features.rolemanagement.admin.service.impl;

import com.nivasafinance.features.rolemanagement.admin.service.AdminUserRoleService;
import com.nivasafinance.features.rolemanagement.mapping.entity.UserRoleMapping;
import com.nivasafinance.features.rolemanagement.mapping.repository.UserRoleMappingRepository;
import com.nivasafinance.features.rolemanagement.role.dto.AddUserRolesRequest;
import com.nivasafinance.features.rolemanagement.role.dto.SetPrimaryRoleRequest;
import com.nivasafinance.features.rolemanagement.role.dto.UserRolesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserRoleServiceImpl implements AdminUserRoleService {
    private final UserRoleMappingRepository userRoleMappingRepository;

    @Override
    public UserRolesResponse getUserRoles(String username) {
        List<UserRoleMapping> mappings = userRoleMappingRepository.findByUsername(username);
        String primary = userRoleMappingRepository.findByUsernameAndIsPrimary(username, true)
                .map(UserRoleMapping::getRole).orElse(null);
        List<String> roles = mappings.stream().map(UserRoleMapping::getRole).collect(Collectors.toList());
        return UserRolesResponse.builder().username(username).roles(roles).primaryRole(primary).build();
        }

    @Override
    @Transactional
    public UserRolesResponse addUserRoles(String username, AddUserRolesRequest request) {
        List<String> roles = request.getRoles() != null ? request.getRoles() : new ArrayList<>();
        for (String role : roles) {
            if (!userRoleMappingRepository.existsByUsernameAndRole(username, role)) {
                UserRoleMapping m = new UserRoleMapping();
                m.setUsername(username);
                m.setRole(role);
                m.setIsPrimary(false);
                userRoleMappingRepository.save(m);
            }
        }
        if (Objects.nonNull(request.getPrimaryRole())) {
            SetPrimaryRoleRequest pr = new SetPrimaryRoleRequest();
            pr.setRole(request.getPrimaryRole());
            setPrimaryRole(username, pr);
        }
        return getUserRoles(username);
    }

    

    @Override
    @Transactional
    public void removeUserRole(String username, String role) {
        Optional<UserRoleMapping> primaryOpt = userRoleMappingRepository.findByUsernameAndIsPrimary(username, true);
        boolean removingPrimary = primaryOpt.map(UserRoleMapping::getRole).filter(role::equals).isPresent();
        userRoleMappingRepository.deleteByUsernameAndRole(username, role);
        if (removingPrimary) {
            List<UserRoleMapping> remaining = userRoleMappingRepository.findByUsername(username);
            if (!remaining.isEmpty()) {
                for (UserRoleMapping m : remaining) {
                    m.setIsPrimary(false);
                }
                UserRoleMapping first = remaining.get(0);
                first.setIsPrimary(true);
                userRoleMappingRepository.saveAll(remaining);
            }
        }
    }

    @Override
    @Transactional
    public UserRolesResponse setPrimaryRole(String username, SetPrimaryRoleRequest request) {
        List<UserRoleMapping> mappings = userRoleMappingRepository.findByUsername(username);
        for (UserRoleMapping m : mappings) {
            m.setIsPrimary(false);
        }
        Optional<UserRoleMapping> target = userRoleMappingRepository.findByUsernameAndRole(username, request.getRole());
        UserRoleMapping primary = target.orElseGet(() -> {
            UserRoleMapping m = new UserRoleMapping();
            m.setUsername(username);
            m.setRole(request.getRole());
            m.setIsPrimary(false);
            return userRoleMappingRepository.save(m);
        });
        primary.setIsPrimary(true);
        userRoleMappingRepository.save(primary);
        userRoleMappingRepository.saveAll(mappings.stream().filter(m -> !m.getId().equals(primary.getId())).collect(Collectors.toList()));
        return getUserRoles(username);
    }
}
