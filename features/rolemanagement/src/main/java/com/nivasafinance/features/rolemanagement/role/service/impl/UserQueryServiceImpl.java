package com.nivasafinance.features.rolemanagement.role.service.impl;

import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.offices.dto.OfficeResponse;
import com.nivasafinance.features.offices.service.OfficeReadService;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.rolemanagement.mapping.entity.UserRoleMapping;
import com.nivasafinance.features.rolemanagement.mapping.repository.UserRoleMappingRepository;
import com.nivasafinance.features.rolemanagement.role.dto.UserAssignmentResponse;
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
    
    @Override
    public List<UserAssignmentResponse> getUsersByOfficeAndRoles(List<String> roles, String officeKey) {
        try {
            if (!ValidationUtils.isNonNull(roles) || roles.isEmpty()) {
                return Collections.emptyList();
            }

            // Use provided officeKey
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

            if (!allowedOfficeKeys.isEmpty() && !allowedOfficeKeys.contains(staffOfficeKey)) {
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

    @Override
    public List<UserAssignmentResponse> getUsersByOfficeAndRolesDownHierarchy(List<String> roles, String officeKey) {
        try {
            if (!ValidationUtils.isNonNull(roles) || roles.isEmpty()) {
                return Collections.emptyList();
            }

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

            // Get only child offices (down hierarchy), not parents
            Set<String> allowedOfficeKeys = getOfficeHierarchyKeysDownOnly(officeKey);
            return usernames.stream()
                    .map(username -> {
                        try {
                            return buildUserAssignmentResponse(username, allowedOfficeKeys);
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted(Comparator.comparing(UserAssignmentResponse::getUsername))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private Set<String> getOfficeHierarchyKeys(String officeKey) {
        Set<String> officeKeys = new HashSet<>();
        
        if (!ValidationUtils.isNonNull(officeKey)) {
            return officeKeys;
        }
        
        // Always include current office
        officeKeys.add(officeKey);

        try {
            OfficeResponse currentOffice = officeReadService.getOfficeByKey(officeKey);
            if (ValidationUtils.isNonNull(currentOffice) && 
                ValidationUtils.isNonNull(currentOffice.getCode())) {

                // 1. Get all child offices (down the hierarchy)
                // This gets current office + all child offices using code prefix
                List<OfficeResponse> childOffices = officeReadService
                    .getOfficesByCodePrefix(currentOffice.getCode());
                
                childOffices.forEach(office -> {
                    if (ValidationUtils.isNonNull(office.getKey())) {
                        officeKeys.add(office.getKey());
                    }
                });
                
                // 2. Get all parent offices (up the hierarchy)
                // This allows reassignment back to the original assigner
                OfficeResponse parentOffice = currentOffice;
                while (ValidationUtils.isNonNull(parentOffice) && 
                       ValidationUtils.isNonNull(parentOffice.getParentId())) {
                    
                    // Get parent office by ID using repository (via reflection to avoid circular dependency)
                    Optional<Object> parentOfficeOpt = getOfficeById(parentOffice.getParentId());
                    if (parentOfficeOpt.isPresent()) {
                        Object parent = parentOfficeOpt.get();
                        String parentKey = getOfficeKey(parent);
                        if (ValidationUtils.isNonNull(parentKey)) {
                            officeKeys.add(parentKey);
                            // Get the full OfficeResponse to continue traversal up the hierarchy
                            parentOffice = officeReadService.getOfficeByKey(parentKey);
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // Return what we have so far (at least the current office)
            // This ensures the method doesn't fail completely if there's an error
        }

        return officeKeys;
    }
    
    private Set<String> getOfficeHierarchyKeysDownOnly(String officeKey) {
        Set<String> officeKeys = new HashSet<>();
        
        if (!ValidationUtils.isNonNull(officeKey)) {
            return officeKeys;
        }
        
        // Always include current office
        officeKeys.add(officeKey);

        try {
            OfficeResponse currentOffice = officeReadService.getOfficeByKey(officeKey);
            if (ValidationUtils.isNonNull(currentOffice) && 
                ValidationUtils.isNonNull(currentOffice.getCode())) {

                // Get all child offices (down the hierarchy only)
                // This gets current office + all child offices using code prefix
                List<OfficeResponse> childOffices = officeReadService
                    .getOfficesByCodePrefix(currentOffice.getCode());
                
                childOffices.forEach(office -> {
                    if (ValidationUtils.isNonNull(office.getKey())) {
                        officeKeys.add(office.getKey());
                    }
                });
                
                // NOTE: We do NOT traverse up the hierarchy (no parent offices)
            }
        } catch (Exception e) {
            // Return what we have so far (at least the current office)
        }

        return officeKeys;
    }
    
    @SuppressWarnings("unchecked")
    private Optional<Object> getOfficeById(Long id) {
        try {
            Object officeRepository = applicationContext.getBean("officeRepository");
            java.lang.reflect.Method findById = officeRepository.getClass().getMethod("findById", Object.class);
            Object result = findById.invoke(officeRepository, id);
            if (result instanceof Optional) {
                return (Optional<Object>) result;
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    private String getOfficeKey(Object office) {
        try {
            java.lang.reflect.Method getKey = office.getClass().getMethod("getKey");
            return (String) getKey.invoke(office);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<UserAssignmentResponse> getUsersByRoles(List<String> roles) {
        return userRoleMappingRepository.findByRoleIn(roles).stream()
                .map(UserRoleMapping::getUsername)
                .map(username -> buildUserAssignmentResponse(username, new HashSet<>()))
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.comparing(UserAssignmentResponse::getUsername))
                .collect(Collectors.toList());
    }
}

