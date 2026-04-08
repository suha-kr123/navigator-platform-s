package com.nivasafinance.features.rolemanagement.permission.service.impl;

import com.nivasafinance.features.rolemanagement.permission.dto.PermissionResponse;
import com.nivasafinance.features.rolemanagement.permission.entity.Permission;
import com.nivasafinance.features.rolemanagement.permission.repository.PermissionReadRepositoryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionReadServiceImplTest {

    @Mock
    private PermissionReadRepositoryWrapper permissionReadRepositoryWrapper;

    @InjectMocks
    private PermissionReadServiceImpl service;

    // ── getPermissionsByRoles ──────────────────────────────────────
    @Test
    void getPermissionsByRoles_nullList_returnsEmpty() {
        // Act
        List<PermissionResponse> result = service.getPermissionsByRoles(null);
        // Assert
        assertTrue(result.isEmpty(), "getPermissionsByRoles should return empty when input roles is null");
        verifyNoInteractions(permissionReadRepositoryWrapper);
    }

    @Test
    void getPermissionsByRoles_emptyList_returnsEmpty() {
        // Act
        List<PermissionResponse> result = service.getPermissionsByRoles(Collections.emptyList());
        // Assert
        assertTrue(result.isEmpty(), "getPermissionsByRoles should return empty when input roles is empty");
        verifyNoInteractions(permissionReadRepositoryWrapper);
    }

    @Test
    void getPermissionsByRoles_mapsEntitiesToResponses() {
        // Arrange
        Permission p = new Permission();
        p.setId(1L);
        p.setName("READ_USERS");
        when(permissionReadRepositoryWrapper.findPermissionsByRoleNames(List.of("ADMIN")))
                .thenReturn(List.of(p));
        // Act
        List<PermissionResponse> result = service.getPermissionsByRoles(List.of("ADMIN"));
        // Assert
        assertEquals(1, result.size(), "getPermissionsByRoles should map each Permission to a PermissionResponse");
        assertEquals(1L, result.get(0).getId(), "Mapped response should carry the entity id");
    }
}

