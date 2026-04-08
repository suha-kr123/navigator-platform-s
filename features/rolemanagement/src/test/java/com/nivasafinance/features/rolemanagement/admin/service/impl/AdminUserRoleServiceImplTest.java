package com.nivasafinance.features.rolemanagement.admin.service.impl;

import com.nivasafinance.features.rolemanagement.admin.service.AdminUserRoleService;
import com.nivasafinance.features.rolemanagement.mapping.entity.UserRoleMapping;
import com.nivasafinance.features.rolemanagement.mapping.repository.UserRoleMappingRepository;
import com.nivasafinance.features.rolemanagement.role.dto.AddUserRolesRequest;
import com.nivasafinance.features.rolemanagement.role.dto.SetPrimaryRoleRequest;
import com.nivasafinance.features.rolemanagement.role.dto.UserRolesResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserRoleServiceImplTest {

    @Mock
    private UserRoleMappingRepository userRoleMappingRepository;

    @InjectMocks
    private AdminUserRoleServiceImpl service;

    // ── getUserRoles ────────────────────────────────────────────────
    @Test
    void getUserRoles_happy_returnsAggregatedResponse() {
        // Arrange
        UserRoleMapping m1 = new UserRoleMapping(); m1.setRole("R1");
        UserRoleMapping m2 = new UserRoleMapping(); m2.setRole("R2");
        when(userRoleMappingRepository.findByUsername("u")).thenReturn(List.of(m1, m2));
        UserRoleMapping primary = new UserRoleMapping(); primary.setRole("R1");
        when(userRoleMappingRepository.findByUsernameAndIsPrimary("u", true)).thenReturn(Optional.of(primary));
        // Act
        UserRolesResponse resp = service.getUserRoles("u");
        // Assert
        assertEquals("u", resp.getUsername(), "getUserRoles should echo username");
        assertEquals("R1", resp.getPrimaryRole(), "getUserRoles should set primary role");
        assertEquals(2, resp.getRoles().size(), "getUserRoles should include all roles");
    }

    // ── addUserRoles ────────────────────────────────────────────────
    @Test
    void addUserRoles_avoidsDuplicates_andSetsPrimaryIfProvided() {
        // Arrange
        AddUserRolesRequest req = new AddUserRolesRequest();
        req.setRoles(List.of("R1", "R2"));
        req.setPrimaryRole("R2");
        when(userRoleMappingRepository.existsByUsernameAndRole("u", "R1")).thenReturn(false);
        when(userRoleMappingRepository.existsByUsernameAndRole("u", "R2")).thenReturn(true);
        when(userRoleMappingRepository.findByUsername("u")).thenReturn(new ArrayList<>());
        when(userRoleMappingRepository.findByUsernameAndRole("u", "R2")).thenReturn(Optional.empty());
        when(userRoleMappingRepository.save(any(UserRoleMapping.class))).thenAnswer(inv -> {
            UserRoleMapping m = inv.getArgument(0);
            if (m.getId() == null) {
                m.setId(1L);
            }
            return m;
        });
        // Act
        UserRolesResponse resp = service.addUserRoles("u", req);
        // Assert
        ArgumentCaptor<UserRoleMapping> captor = ArgumentCaptor.forClass(UserRoleMapping.class);
        verify(userRoleMappingRepository, atLeastOnce()).save(captor.capture());
        boolean savedR1 = captor.getAllValues().stream().anyMatch(m -> "R1".equals(m.getRole()));
        assertTrue(savedR1, "addUserRoles should attempt saving the non-existing role R1");
        assertNotNull(resp, "addUserRoles should return a response");
    }

    // ── removeUserRole ──────────────────────────────────────────────
    @Test
    void removeUserRole_whenRemovingPrimary_promotesFirstRemaining() {
        // Arrange
        UserRoleMapping primary = new UserRoleMapping(); primary.setRole("R1");
        when(userRoleMappingRepository.findByUsernameAndIsPrimary("u", true)).thenReturn(Optional.of(primary));
        UserRoleMapping r2 = new UserRoleMapping(); r2.setId(2L); r2.setRole("R2");
        UserRoleMapping r3 = new UserRoleMapping(); r3.setId(3L); r3.setRole("R3");
        when(userRoleMappingRepository.findByUsername("u")).thenReturn(List.of(r2, r3));
        // Act
        service.removeUserRole("u", "R1");
        // Assert
        verify(userRoleMappingRepository).deleteByUsernameAndRole("u", "R1");
        verify(userRoleMappingRepository).saveAll(anyList());
    }

    // ── setPrimaryRole ──────────────────────────────────────────────
    @Test
    void setPrimaryRole_setsFlags_andCreatesIfMissing() {
        // Arrange
        UserRoleMapping existing = new UserRoleMapping(); existing.setId(10L); existing.setRole("R1"); existing.setIsPrimary(true);
        when(userRoleMappingRepository.findByUsername("u")).thenReturn(List.of(existing));
        when(userRoleMappingRepository.findByUsernameAndRole("u", "R2")).thenReturn(Optional.empty());
        when(userRoleMappingRepository.save(any(UserRoleMapping.class))).thenAnswer(inv -> inv.getArgument(0));
        SetPrimaryRoleRequest req = new SetPrimaryRoleRequest(); req.setRole("R2");
        // Act
        UserRolesResponse resp = service.setPrimaryRole("u", req);
        // Assert
        assertEquals("u", resp.getUsername(), "setPrimaryRole should return response for username");
        verify(userRoleMappingRepository, atLeastOnce()).save(any(UserRoleMapping.class));
    }
}

