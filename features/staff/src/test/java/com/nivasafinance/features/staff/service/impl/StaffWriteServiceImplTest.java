package com.nivasafinance.features.staff.service.impl;

import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.common.exception.ValidationException;
import com.nivasafinance.features.offices.service.OfficeReadService;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.person.service.PersonReadService;
import com.nivasafinance.features.referral.enums.EntityType;
import com.nivasafinance.features.referral.service.ReferralCodeRegistryService;
import com.nivasafinance.features.rolemanagement.role.service.UserRoleService;
import com.nivasafinance.features.staff.dto.StaffCreateRequest;
import com.nivasafinance.features.staff.dto.StaffCreateRequest.Role;
import com.nivasafinance.features.staff.entity.Staff;
import com.nivasafinance.features.staff.repository.StaffRepositoryWrapper;
import com.nivasafinance.features.usermanagement.dto.UserCreateRequest;
import com.nivasafinance.features.usermanagement.dto.UserResponse;
import com.nivasafinance.features.usermanagement.entity.User;
import com.nivasafinance.features.usermanagement.repository.UserRepository;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import com.nivasafinance.features.usermanagement.service.UserWriteService;
import com.nivasafinance.integrations.framework.ServiceFactory;
import com.nivasafinance.integrations.framework.config.BusinessContext;
import com.nivasafinance.integrations.framework.config.ThirdPartyServiceList;
import com.nivasafinance.services.authentication.AuthenticationHandler;
import com.nivasafinance.services.authentication.dto.AuthCreateUserRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.*;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffWriteServiceImplTest {

    @Mock private StaffRepositoryWrapper staffRepositoryWrapper;
    @Mock private UserReadService userReadService;
    @Mock private UserWriteService userWriteService;
    @Mock private OfficeReadService officeReadService;
    @Mock private UserRoleService userRoleService;
    @Mock private ReferralCodeRegistryService referralCodeRegistryService;
    @Mock private PersonReadService personReadService;
    @Mock private UserRepository userRepository;
    @Mock private MessageSource messageSource;
    @Mock private ServiceFactory<AuthenticationHandler> authenticationServiceFactory;
    @Mock private AuthenticationHandler authenticationHandler;

    @InjectMocks
    private StaffWriteServiceImpl service;

    private StaffCreateRequest baseRequestWithPrimaryMobile(String officeKey, String username, String mobile, boolean includePassword) {
        StaffCreateRequest req = new StaffCreateRequest();
        req.setOfficeKey(officeKey);
        UserCreateRequest u = UserCreateRequest.builder().username(username).build();
        com.nivasafinance.features.person.dto.PersonCreateRequest p = com.nivasafinance.features.person.dto.PersonCreateRequest.builder().build();
        List<MobileNumberDetails> numbers = new ArrayList<>();
        MobileNumberDetails m = new MobileNumberDetails(); m.setNumber(mobile); m.setIsPrimary(true);
        numbers.add(m);
        p.setMobileNumbers(numbers);
        u.setPerson(p);
        req.setUser(u);
        if (includePassword) req.setPassword("secret");
        return req;
    }

    // ── createStaff ─────────────────────────────────────────────────
    @Test
    void createStaff_missingPersonOrMobiles_throwsBadRequest() {
        // Arrange
        StaffCreateRequest req = new StaffCreateRequest();
        req.setOfficeKey("OK");
        req.setUser(UserCreateRequest.builder().username("ann").build());
        // Act + Assert
        assertThrows(BadRequestException.class,
                () -> service.createStaff(req),
                "createStaff should enforce person with mobile numbers");
    }

    @Test
    void createStaff_noPrimaryMobile_throwsBadRequest() {
        // Arrange
        StaffCreateRequest req = new StaffCreateRequest();
        req.setOfficeKey("OK");
        UserCreateRequest u = UserCreateRequest.builder().username("ann").build();
        com.nivasafinance.features.person.dto.PersonCreateRequest p = com.nivasafinance.features.person.dto.PersonCreateRequest.builder().build();
        List<MobileNumberDetails> numbers = new ArrayList<>();
        MobileNumberDetails m = new MobileNumberDetails(); m.setNumber("999"); m.setIsPrimary(false);
        numbers.add(m);
        p.setMobileNumbers(numbers);
        u.setPerson(p);
        req.setUser(u);
        // Act + Assert
        assertThrows(BadRequestException.class,
                () -> service.createStaff(req),
                "createStaff should enforce at least one primary mobile");
    }

    @Test
    void createStaff_personExists_withExistingUserAndStaff_throwsAlreadyExists() {
        // Arrange
        StaffCreateRequest req = baseRequestWithPrimaryMobile("OK", "ann", "999", false);
        when(personReadService.findPersonByPrimaryMobile("999")).thenReturn(Optional.of(PersonResponse.builder().id(7L).build()));
        when(userRepository.findByPerson_Id(7L)).thenReturn(Optional.of(new User(){{
            setId(11L);
        }}));
        when(staffRepositoryWrapper.findByUserId(11L)).thenReturn(Optional.of(new Staff()));
        // Act + Assert
        assertThrows(RuntimeException.class,
                () -> service.createStaff(req),
                "createStaff should throw domain exception when staff already exists for person");
    }

    @Test
    void createStaff_personExists_withoutStaff_createsUserForExistingPerson_andStaff() {
        // Arrange
        StaffCreateRequest req = baseRequestWithPrimaryMobile("OK", "ann", "999", false);
        when(personReadService.findPersonByPrimaryMobile("999")).thenReturn(Optional.of(PersonResponse.builder().id(7L).build()));
        when(userRepository.findByPerson_Id(7L)).thenReturn(Optional.empty());
        when(userWriteService.createUserForExistingPerson(any(UserCreateRequest.class), eq(7L)))
                .thenReturn(UserResponse.builder().id(11L).username("ann").build());
        when(referralCodeRegistryService.generateReferralCode(eq(EntityType.STAFF), any(UUID.class)))
                .thenReturn(com.nivasafinance.features.referral.dto.ReferralCodeRegistryResponse.builder().referralCode("RC").build());
        when(staffRepositoryWrapper.saveWithException(any(Staff.class))).thenAnswer(inv -> {
            Staff s = inv.getArgument(0); s.setId(100L); return s;
        });
        // Act
        service.createStaff(req);
        // Assert
        verify(userWriteService).createUserForExistingPerson(any(UserCreateRequest.class), eq(7L));
        verify(staffRepositoryWrapper).saveWithException(any(Staff.class));
    }

    @Test
    void createStaff_personNotFound_createsUser_andStaff_andMayCreateAuthUserOnPassword() {
        // Arrange
        StaffCreateRequest req = baseRequestWithPrimaryMobile("OK", "ann", "999", true);
        when(personReadService.findPersonByPrimaryMobile("999")).thenReturn(Optional.empty());
        when(userWriteService.createUser(any(UserCreateRequest.class)))
                .thenReturn(UserResponse.builder().id(11L).username("ann").build());
        when(referralCodeRegistryService.generateReferralCode(eq(EntityType.STAFF), any(UUID.class)))
                .thenReturn(com.nivasafinance.features.referral.dto.ReferralCodeRegistryResponse.builder().referralCode("RC").build());
        when(staffRepositoryWrapper.saveWithException(any(Staff.class))).thenAnswer(inv -> inv.getArgument(0));
        when(authenticationServiceFactory.getHandler(ThirdPartyServiceList.AUTHENTICATION)).thenReturn(authenticationHandler);
        // Act
        service.createStaff(req);
        // Assert
        verify(userWriteService).createUser(any(UserCreateRequest.class));
        verify(authenticationHandler).createUser(any(AuthCreateUserRequest.class), any(BusinessContext.class));
    }

    @Test
    void createStaff_multiplePrimaryRoles_throwsValidation() {
        // Arrange
        StaffCreateRequest req = baseRequestWithPrimaryMobile("OK", "ann", "999", false);
        Role r1 = new Role(); r1.setRolename("R1"); r1.setIsPrimary(true);
        Role r2 = new Role(); r2.setRolename("R2"); r2.setIsPrimary(true);
        req.setRoles(List.of(r1, r2));
        // Act + Assert
        assertThrows(ValidationException.class,
                () -> service.createStaff(req),
                "createStaff should enforce at most one primary role");
    }

    // ── mapUserToOffice ─────────────────────────────────────────────
    @Test
    void mapUserToOffice_whenStaffExists_updatesOfficeKey() {
        // Arrange
        when(userReadService.getUserByUsername("ann")).thenReturn(UserResponse.builder().id(11L).username("ann").build());
        Staff existing = new Staff(); existing.setUserId(11L);
        when(staffRepositoryWrapper.findByUserId(11L)).thenReturn(Optional.of(existing));
        // Act
        service.mapUserToOffice("ann", "OK");
        // Assert
        verify(staffRepositoryWrapper).saveWithException(existing);
    }

    @Test
    void mapUserToOffice_whenStaffNotExists_createsNewStaff() {
        // Arrange
        when(userReadService.getUserByUsername("ann")).thenReturn(UserResponse.builder().id(11L).username("ann").build());
        when(staffRepositoryWrapper.findByUserId(11L)).thenReturn(Optional.empty());
        when(referralCodeRegistryService.generateReferralCode(eq(EntityType.STAFF), any(UUID.class)))
                .thenReturn(com.nivasafinance.features.referral.dto.ReferralCodeRegistryResponse.builder().referralCode("RC").build());
        // Act
        service.mapUserToOffice("ann", "OK");
        // Assert
        verify(staffRepositoryWrapper).saveWithException(any(Staff.class));
    }
}

