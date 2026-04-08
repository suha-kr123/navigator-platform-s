package com.nivasafinance.features.staff.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.features.offices.dto.OfficeResponse;
import com.nivasafinance.features.offices.service.OfficeReadService;
import com.nivasafinance.features.staff.dto.StaffResponse;
import com.nivasafinance.features.staff.entity.Staff;
import com.nivasafinance.features.staff.exception.StaffExceptionFactory;
import com.nivasafinance.features.staff.repository.StaffRepositoryWrapper;
import com.nivasafinance.features.usermanagement.dto.UserResponse;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffReadServiceImplTest {

    @Mock private StaffRepositoryWrapper staffRepositoryWrapper;
    @Mock private UserReadService userReadService;
    @Mock private OfficeReadService officeReadService;
    @Mock private MessageSource messageSource;

    @InjectMocks
    private StaffReadServiceImpl service;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    // ── getStaff ────────────────────────────────────────────────────
    @Test
    void getStaff_withOfficeKey_validatesOffice_andMapsResults() {
        // Arrange
        PaginationRequest pr = new PaginationRequest();
        pr.setOffset(0); pr.setLimit(10); pr.setSortBy("id"); pr.setSortDirection("DESC");
        Staff s = new Staff(); s.setUserId(1L); s.setOfficeKey("OK");
        PaginatedResponse<Staff> paged = new PaginatedResponse<>(List.of(s), new PaginationInfo(0,10,1,1,0,false,false));
        when(staffRepositoryWrapper.findStaff("OK", "ann", pr)).thenReturn(paged);
        when(userReadService.getUserById(1L)).thenReturn(UserResponse.builder().id(1L).username("ann").build());
        OfficeResponse office = new OfficeResponse();
        office.setKey("OK");
        office.setName("Office");
        when(officeReadService.getOfficeByKey("OK")).thenReturn(office);
        // Act
        PaginatedResponse<StaffResponse> out = service.getStaff("OK", "ann", pr);
        // Assert
        assertEquals(1, out.getContent().size(), "getStaff should map repository results");
        verify(officeReadService, times(2)).getOfficeByKey("OK");
    }

    // ── getCurrentStaff ─────────────────────────────────────────────
    @Test
    void getCurrentStaff_noUserContext_throwsNoCurrentUser() {
        // Arrange
        UserContext.clear();
        // Act + Assert
        assertThrows(RuntimeException.class, // concrete exception from factory is domain-specific
                () -> service.getCurrentStaff(),
                "getCurrentStaff should throw when there is no current user");
    }

    @Test
    void getCurrentStaff_happyPath_returnsMappedResponse() {
        // Arrange
        UserContext.setUsername("ann");
        when(userReadService.getUserByUsername("ann")).thenReturn(UserResponse.builder().id(1L).username("ann").build());
        Staff staff = new Staff(); staff.setUserId(1L); staff.setOfficeKey("OK");
        when(staffRepositoryWrapper.findByUserIdWithException(1L)).thenReturn(staff);
        OfficeResponse office2 = new OfficeResponse();
        office2.setKey("OK");
        office2.setName("Office");
        when(officeReadService.getOfficeByKey("OK")).thenReturn(office2);
        // Act
        StaffResponse out = service.getCurrentStaff();
        // Assert
        assertEquals("ann", out.getUserResponse().getUsername(), "getCurrentStaff should map user response");
    }

    // ── getStaffByOfficeKeys ────────────────────────────────────────
    @Test
    void getStaffByOfficeKeys_nullOrEmpty_returnsEmpty() {
        // Act
        List<StaffResponse> out1 = service.getStaffByOfficeKeys(null);
        List<StaffResponse> out2 = service.getStaffByOfficeKeys(Collections.emptyList());
        // Assert
        assertTrue(out1.isEmpty(), "getStaffByOfficeKeys should return empty for null input");
        assertTrue(out2.isEmpty(), "getStaffByOfficeKeys should return empty for empty input");
        verifyNoInteractions(staffRepositoryWrapper);
    }

    @Test
    void getStaffByOfficeKeys_happyPath_mapsUsers() {
        // Arrange
        Staff s = new Staff(); s.setUserId(1L); s.setOfficeKey("OK");
        when(staffRepositoryWrapper.findAllByOfficeKeys(List.of("OK"))).thenReturn(List.of(s));
        when(userReadService.getUserById(1L)).thenReturn(UserResponse.builder().id(1L).username("ann").build());
        OfficeResponse office = new OfficeResponse();
        office.setKey("OK");
        office.setName("Office");
        when(officeReadService.getOfficeByKey("OK")).thenReturn(office);
        // Act
        List<StaffResponse> out = service.getStaffByOfficeKeys(List.of("OK"));
        // Assert
        assertEquals(1, out.size(), "getStaffByOfficeKeys should map one staff");
    }

    // ── getStaffByIdentifier ────────────────────────────────────────
    @Test
    void getStaffByIdentifier_null_returnsEmptyOptional() {
        // Act
        Optional<StaffResponse> out = service.getStaffByIdentifier(null);
        // Assert
        assertTrue(out.isEmpty(), "getStaffByIdentifier should return empty when identifier is null");
        verifyNoInteractions(staffRepositoryWrapper);
    }

    @Test
    void getStaffByIdentifier_present_mapsUser() {
        // Arrange
        Staff s = new Staff(); s.setUserId(1L); s.setOfficeKey("OK"); s.setIdentifier(UUID.randomUUID());
        when(staffRepositoryWrapper.findByIdentifier(any(UUID.class))).thenReturn(Optional.of(s));
        when(userReadService.getUserById(1L)).thenReturn(UserResponse.builder().id(1L).username("ann").build());
        OfficeResponse office = new OfficeResponse();
        office.setKey("OK");
        office.setName("Office");
        when(officeReadService.getOfficeByKey("OK")).thenReturn(office);
        // Act
        Optional<StaffResponse> out = service.getStaffByIdentifier(UUID.randomUUID());
        // Assert
        assertTrue(out.isPresent(), "getStaffByIdentifier should map to response when found");
    }
}

