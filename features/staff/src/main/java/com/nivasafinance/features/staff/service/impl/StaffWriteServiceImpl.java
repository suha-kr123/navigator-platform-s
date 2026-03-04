package com.nivasafinance.features.staff.service.impl;

import com.nivasafinance.features.staff.dto.StaffCreateRequest;
import com.nivasafinance.features.staff.dto.StaffResponse;
import com.nivasafinance.features.staff.entity.Staff;
import com.nivasafinance.features.staff.repository.StaffRepositoryWrapper;
import com.nivasafinance.features.staff.service.StaffWriteService;
import com.nivasafinance.features.usermanagement.dto.UserResponse;
import com.nivasafinance.features.usermanagement.enums.UserStatus;
import com.nivasafinance.features.usermanagement.dto.UserCreateRequest;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import com.nivasafinance.features.usermanagement.service.UserWriteService;
import com.nivasafinance.features.offices.service.OfficeReadService;
import com.nivasafinance.features.referral.enums.EntityType;
import com.nivasafinance.features.referral.service.ReferralCodeRegistryService;
import com.nivasafinance.features.rolemanagement.role.service.UserRoleService;
import com.nivasafinance.features.staff.dto.StaffCreateRequest.Role;
import com.nivasafinance.features.person.service.PersonReadService;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.usermanagement.repository.UserRepository;
import com.nivasafinance.features.staff.exception.StaffExceptionFactory;
import com.nivasafinance.common.exception.ValidationException;
import com.nivasafinance.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class StaffWriteServiceImpl implements StaffWriteService {

    private final StaffRepositoryWrapper staffRepositoryWrapper;
    private final UserReadService userReadService;
    private final UserWriteService userWriteService;
    private final OfficeReadService officeReadService;
    private final UserRoleService userRoleService;
    private final ReferralCodeRegistryService referralCodeRegistryService;
    private final PersonReadService personReadService;
    private final UserRepository userRepository;
    private final MessageSource messageSource;

    @Override
    public StaffResponse createStaff(StaffCreateRequest request) {
        officeReadService.getOfficeByKey(request.getOfficeKey());

        UserCreateRequest userRequest = request.getUser();

        userReadService.checkForUserNameAvailability(userRequest.getUsername());

        UserResponse createdUser;
        
        // Check if person already exists with the same primary mobile number
        // Primary mobile number is mandatory
        if (userRequest.getPerson() == null || userRequest.getPerson().getMobileNumbers() == null) {
            throw new BadRequestException("Person with mobile numbers is required");
        }
        
        String primaryMobile = extractPrimaryMobileNumber(userRequest.getPerson().getMobileNumbers());
        if (primaryMobile == null) {
            throw new BadRequestException("One Mobile Number is required to be marked as primary");
        }
        
        Optional<com.nivasafinance.features.person.dto.PersonResponse> existingPerson = 
            personReadService.findPersonByPrimaryMobile(primaryMobile);
        
        if (existingPerson.isPresent()) {
            // Person exists - check if staff already exists for this person
            Long personId = existingPerson.get().getId();
            
            // Find user linked to this person
            Optional<com.nivasafinance.features.usermanagement.entity.User> existingUser = 
                userRepository.findByPerson_Id(personId);
            
            if (existingUser.isPresent()) {
                // User exists for this person - check if staff exists
                Long existingUserId = existingUser.get().getId();
                Optional<com.nivasafinance.features.staff.entity.Staff> existingStaff = 
                    staffRepositoryWrapper.findByUserId(existingUserId);
                
                if (existingStaff.isPresent()) {
                    // Staff already exists for this person - throw error
                    throw StaffExceptionFactory.alreadyExists(
                        existingUserId, 
                        request.getOfficeKey(), 
                        messageSource
                    );
                }
            }
            
            // Person exists but no staff found - create new user with provided username and staff
            UserCreateRequest userCreateRequest = UserCreateRequest.builder()
                    .username(userRequest.getUsername())
                    .status(userRequest.getStatus() != null ? userRequest.getStatus() : UserStatus.ACTIVE)
                    .build();
            createdUser = userWriteService.createUserForExistingPerson(
                userCreateRequest, 
                personId
            );
        } else {
            // Create new person
            UserCreateRequest userCreateRequest = UserCreateRequest.builder()
                    .username(userRequest.getUsername())
                    .status(userRequest.getStatus() != null ? userRequest.getStatus() : UserStatus.ACTIVE)
                    .person(userRequest.getPerson())
                    .build();
            createdUser = userWriteService.createUser(userCreateRequest);
        }

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            validateOnlyOnePrimaryRole(request.getRoles());
            for (Role role : request.getRoles()) {
                userRoleService.saveRolesForUsername(
                        createdUser.getUsername(),
                        role.getRolename(),
                        role.getIsPrimary()
                );
            }
        }

        Staff staff = new Staff();
        staff.setIdentifier(UUID.randomUUID());
        staff.setUserId(createdUser.getId());
        staff.setOfficeKey(request.getOfficeKey());
        staff.setReferralCode(referralCodeRegistryService.generateReferralCode(EntityType.STAFF, staff.getIdentifier()).getReferralCode());
        Staff saved = staffRepositoryWrapper.saveWithException(staff);
        return mapToResponse(saved, createdUser);
    }

    private StaffResponse mapToResponse(Staff staff, UserResponse userResponse) {
        UserResponse resolvedUser = userResponse != null
                ? userResponse
                : userReadService.getUserById(staff.getUserId());

        return StaffResponse.builder()
                .id(staff.getId())
                .identifier(staff.getIdentifier())
                .officeKey(staff.getOfficeKey())
                .userResponse(resolvedUser)
                .build();
    }

    private void validateOnlyOnePrimaryRole(List<Role> roles) {
        long primaryRoleCount = roles.stream()
                .filter(role -> role.getIsPrimary() != null && role.getIsPrimary())
                .count();
        
        if (primaryRoleCount > 1) {
            throw new ValidationException("Only one role can be marked as primary");
        }
    }

    private String extractPrimaryMobileNumber(List<MobileNumberDetails> mobileNumbers) {
        if (mobileNumbers == null || mobileNumbers.isEmpty()) {
            return null;
        }
        
        return mobileNumbers.stream()
                .filter(m -> m.getIsPrimary() != null && m.getIsPrimary())
                .map(MobileNumberDetails::getNumber)
                .findFirst()
                .orElse(null);
    }
}

