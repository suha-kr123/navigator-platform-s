package com.nivasafinance.features.rolemanagement.role.service.impl;

import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.offices.dto.OfficeResponse;
import com.nivasafinance.features.offices.service.OfficeReadService;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.rolemanagement.mapping.entity.UserRoleMapping;
import com.nivasafinance.features.rolemanagement.mapping.repository.UserRoleMappingRepository;
import com.nivasafinance.features.rolemanagement.role.dto.UserAssignmentResponse;
import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.features.rolemanagement.role.service.UserQueryService;
import com.nivasafinance.features.rolemanagement.role.service.UserRoleService;
import com.nivasafinance.features.usermanagement.dto.UserResponse;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserQueryServiceImpl implements UserQueryService, ApplicationContextAware {

    private final UserRoleMappingRepository userRoleMappingRepository;
    private final UserReadService userReadService;
    private final OfficeReadService officeReadService;
    private final UserRoleService userRoleService;
    private ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    @SuppressWarnings("unchecked")
    private Optional<Object> findStaffByUserId(Long userId) {
        try {
            Object staffRepository = applicationContext.getBean("staffRepository");
            java.lang.reflect.Method findByUserId = staffRepository.getClass().getMethod("findByUserId", Long.class);
            Object result = findByUserId.invoke(staffRepository, userId);
            if (result instanceof Optional) {
                return (Optional<Object>) result;
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    private String getStaffOfficeKey(Object staff) {
        try {
            java.lang.reflect.Method getOfficeKey = staff.getClass().getMethod("getOfficeKey");
            return (String) getOfficeKey.invoke(staff);
        } catch (Exception e) {
            return null;
        }
    }
    
    private String getCurrentStaffOfficeKey() {
        try {
            String currentUsername = UserContext.getUsername();
            if (!ValidationUtils.isNonNull(currentUsername)) {
                return null;
            }
            
            UserResponse user = userReadService.getUserByUsername(currentUsername);
            if (!ValidationUtils.isNonNull(user)) {
                return null;
            }
            
            Optional<Object> staffOpt = findStaffByUserId(user.getId());
            if (staffOpt.isEmpty()) {
                return null;
            }
            
            return getStaffOfficeKey(staffOpt.get());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<UserAssignmentResponse> getUsersByOfficeAndRoles(List<String> roles) {
        try {
            if (!ValidationUtils.isNonNull(roles) || roles.isEmpty()) {
                return Collections.emptyList();
            }

            // Fetch officeKey from current staff using reflection to avoid circular dependency
            String officeKey = getCurrentStaffOfficeKey();
            if (!ValidationUtils.isNonNull(officeKey)) {
                return Collections.emptyList();
            }

            List<UserRoleMapping> roleMappings = userRoleMappingRepository.findByRoleIn(roles);
            if (!ValidationUtils.isNonNull(roleMappings) || roleMappings.isEmpty()) {
                return Collections.emptyList();
            }

            Set<String> usernames = roleMappings.stream()
                    .map(UserRoleMapping::getUsername)
                    .filter(ValidationUtils::isNonNull)
                    .collect(Collectors.toSet());

            if (usernames.isEmpty()) {
                return Collections.emptyList();
            }

            Set<String> allowedOfficeKeys = getOfficeHierarchyKeys(officeKey);
            return usernames.stream()
                    .map(username -> {
                        try {
                            return buildUserAssignmentResponse(username, allowedOfficeKeys);
                        } catch (Exception e) {
                            // Log individual failures but continue processing
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted(Comparator.comparing(UserAssignmentResponse::getUsername))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // Log and return empty list instead of propagating exception
            // This prevents transaction rollback
            return Collections.emptyList();
        }
    }

    private UserAssignmentResponse buildUserAssignmentResponse(String username, Set<String> allowedOfficeKeys) {
        if (!ValidationUtils.isNonNull(username)) {
            return null;
        }
        
        try {
            UserResponse user = userReadService.getUserByUsername(username);
            if (!ValidationUtils.isNonNull(user) || user.getStatus() != com.nivasafinance.features.usermanagement.enums.UserStatus.ACTIVE) {
                return null;
            }

            Optional<Object> staffOpt = findStaffByUserId(user.getId());
            if (staffOpt.isEmpty()) {
                return null;
            }

            Object staff = staffOpt.get();
            String staffOfficeKey = getStaffOfficeKey(staff);

            if (!allowedOfficeKeys.contains(staffOfficeKey)) {
                return null;
            }

            OfficeResponse office = null;
            String officeName = null;
            if (ValidationUtils.isNonNull(staffOfficeKey)) {
                try {
                    office = officeReadService.getOfficeByKey(staffOfficeKey);
                    officeName = ValidationUtils.isNonNull(office) ? office.getName() : null;
                } catch (Exception e) {
                    return null;
                }
            }

            List<String> roles = userRoleService.getRolesByUsername(username);

            String name = null;
            if (ValidationUtils.isNonNull(user.getPersonResponse())) {
                PersonResponse person = user.getPersonResponse();
                if (ValidationUtils.isNonNull(person.getFirstName()) || ValidationUtils.isNonNull(person.getLastName())) {
                    String firstName = ValidationUtils.isNonNull(person.getFirstName()) ? person.getFirstName() : "";
                    String lastName = ValidationUtils.isNonNull(person.getLastName()) ? " " + person.getLastName() : "";
                    String fullName = (firstName + lastName).trim();
                    if (ValidationUtils.isNonNull(fullName) && !fullName.isEmpty()) {
                        name = fullName;
                    }
                }
            }
            if (!ValidationUtils.isNonNull(name)) {
                name = username;
            }

            return UserAssignmentResponse.builder()
                    .username(username)
                    .name(name)
                    .officeKey(staffOfficeKey)
                    .officeName(officeName)
                    .roles(roles)
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

    private Set<String> getOfficeHierarchyKeys(String officeKey) {
        Set<String> officeKeys = new HashSet<>();
        
        if (!ValidationUtils.isNonNull(officeKey)) {
            return officeKeys;
        }
        
        officeKeys.add(officeKey);

        try {
            OfficeResponse currentOffice = officeReadService.getOfficeByKey(officeKey);
            if (ValidationUtils.isNonNull(currentOffice) && 
                ValidationUtils.isNonNull(currentOffice.getCode())) {

                List<OfficeResponse> officesInHierarchy = officeReadService
                    .getOfficesByCodePrefix(currentOffice.getCode());
                
                officesInHierarchy.forEach(office -> {
                    if (ValidationUtils.isNonNull(office.getKey())) {
                        officeKeys.add(office.getKey());
                    }
                });
            }
        } catch (Exception e) {
            // Return what we have so far (at least the current office)
            // This ensures the method doesn't fail completely if there's an error
        }

        return officeKeys;
    }
}

