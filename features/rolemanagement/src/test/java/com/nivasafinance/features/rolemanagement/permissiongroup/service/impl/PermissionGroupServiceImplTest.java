package com.nivasafinance.features.rolemanagement.permissiongroup.service.impl;

import com.nivasafinance.features.rolemanagement.mapping.entity.RolePermissionGroupMapping;
import com.nivasafinance.features.rolemanagement.mapping.repository.RolePermissionGroupMappingRepositoryWrapper;
import com.nivasafinance.features.rolemanagement.permissiongroup.dto.PermissionGroupResponse;
import com.nivasafinance.features.rolemanagement.permissiongroup.entity.PermissionGroup;
import com.nivasafinance.features.rolemanagement.permissiongroup.repository.PermissionGroupRepositoryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionGroupServiceImplTest {

    @Mock
    private PermissionGroupRepositoryWrapper permissionGroupRepositoryWrapper;
    @Mock
    private RolePermissionGroupMappingRepositoryWrapper rolePermissionGroupMappingRepositoryWrapper;

    @InjectMocks
    private PermissionGroupServiceImpl service;

    // ── getPermissionGroupsbyRoles ─────────────────────────────────
    @Test
    void getPermissionGroupsbyRoles_nullRoles_returnsEmpty() {
        // Act
        List<PermissionGroupResponse> result = service.getPermissionGroupsbyRoles(null);
        // Assert
        assertTrue(result.isEmpty(), "getPermissionGroupsbyRoles should return empty when roles is null");
        verifyNoInteractions(permissionGroupRepositoryWrapper, rolePermissionGroupMappingRepositoryWrapper);
    }

    @Test
    void getPermissionGroupsbyRoles_noMappings_returnsEmpty() {
        // Arrange
        when(rolePermissionGroupMappingRepositoryWrapper.findByRoleIn(List.of("ADMIN")))
                .thenReturn(new ArrayList<>());
        // Act
        List<PermissionGroupResponse> result = service.getPermissionGroupsbyRoles(List.of("ADMIN"));
        // Assert
        assertTrue(result.isEmpty(), "getPermissionGroupsbyRoles should return empty when there are no mappings");
    }

    @Test
    void getPermissionGroupsbyRoles_mapsIdsToResponses() {
        // Arrange
        RolePermissionGroupMapping m = new RolePermissionGroupMapping();
        m.setPermissionGroupId(5L);
        when(rolePermissionGroupMappingRepositoryWrapper.findByRoleIn(List.of("ADMIN")))
                .thenReturn(List.of(m));
        PermissionGroup group = new PermissionGroup();
        group.setId(5L);
        group.setName("Core");
        when(permissionGroupRepositoryWrapper.findById(5L)).thenReturn(group);
        // Act
        List<PermissionGroupResponse> result = service.getPermissionGroupsbyRoles(List.of("ADMIN"));
        // Assert
        assertEquals(1, result.size(), "getPermissionGroupsbyRoles should map group ids to responses");
        assertEquals(5L, result.get(0).getId(), "Mapped response should carry group id");
        assertEquals("Core", result.get(0).getName(), "Mapped response should carry group name");
    }
}

