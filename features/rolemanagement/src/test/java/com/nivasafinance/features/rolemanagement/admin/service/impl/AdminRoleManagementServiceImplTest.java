package com.nivasafinance.features.rolemanagement.admin.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.rolemanagement.permission.dto.PermissionResponse;
import com.nivasafinance.features.rolemanagement.permission.entity.Permission;
import com.nivasafinance.features.rolemanagement.permission.repository.PermissionReadRepositoryWrapper;
import com.nivasafinance.features.rolemanagement.permission.repository.PermissionRepositoryWrapper;
import com.nivasafinance.features.rolemanagement.permissiongroup.dto.PermissionGroupResponse;
import com.nivasafinance.features.rolemanagement.permissiongroup.entity.PermissionGroup;
import com.nivasafinance.features.rolemanagement.permissiongroup.repository.PermissionGroupRepositoryWrapper;
import com.nivasafinance.features.rolemanagement.mapping.entity.RolePermissionGroupMapping;
import com.nivasafinance.features.rolemanagement.mapping.entity.RolePermissionMapping;
import com.nivasafinance.features.rolemanagement.mapping.repository.PermissionGroupMappingRepository;
import com.nivasafinance.features.rolemanagement.mapping.repository.RolePermissionGroupMappingRepository;
import com.nivasafinance.features.rolemanagement.mapping.repository.RolePermissionMappingRepository;
import com.nivasafinance.features.rolemanagement.permission.dto.UpdateRolePermissionsRequest;
import com.nivasafinance.features.rolemanagement.permissiongroup.dto.PermissionGroupWithPermissionsResponse;
import com.nivasafinance.features.rolemanagement.permissiongroup.dto.UpdateRolePermissionGroupsRequest;
import com.nivasafinance.features.rolemanagement.role.dto.CreateRoleRequest;
import com.nivasafinance.features.rolemanagement.role.dto.RoleResponse;
import com.nivasafinance.features.rolemanagement.role.dto.RolePermissionGroupsUpdateResponse;
import com.nivasafinance.features.rolemanagement.role.dto.RolePermissionsUpdateResponse;
import com.nivasafinance.features.rolemanagement.role.entity.Role;
import com.nivasafinance.features.rolemanagement.role.repository.RoleRepositoryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminRoleManagementServiceImplTest {

    @Mock private PermissionRepositoryWrapper permissionRepositoryWrapper;
    @Mock private PermissionGroupRepositoryWrapper permissionGroupRepositoryWrapper;
    @Mock private PermissionReadRepositoryWrapper permissionReadRepositoryWrapper;
    @Mock private RoleRepositoryWrapper roleRepositoryWrapper;
    @Mock private RolePermissionMappingRepository rolePermissionMappingRepository;
    @Mock private RolePermissionGroupMappingRepository rolePermissionGroupMappingRepository;
    @Mock private PermissionGroupMappingRepository permissionGroupMappingRepository;

    @InjectMocks
    private AdminRoleManagementServiceImpl service;

    // ── getAllPermissions ───────────────────────────────────────────
    @Test
    void getAllPermissions_mapsResponses() {
        // Arrange
        Permission p = new Permission(); p.setId(1L); p.setName("READ");
        when(permissionRepositoryWrapper.findAll()).thenReturn(List.of(p));
        // Act
        List<PermissionResponse> out = service.getAllPermissions();
        // Assert
        assertEquals(1, out.size(), "getAllPermissions should map repository results to responses");
    }

    // ── getAllPermissionGroups ─────────────────────────────────────
    @Test
    void getAllPermissionGroups_mapsResponses() {
        // Arrange
        PermissionGroup g = new PermissionGroup(); g.setId(2L); g.setName("Core");
        when(permissionGroupRepositoryWrapper.findAll()).thenReturn(List.of(g));
        // Act
        List<PermissionGroupResponse> out = service.getAllPermissionGroups();
        // Assert
        assertEquals(1, out.size(), "getAllPermissionGroups should map to responses");
        assertEquals("Core", out.get(0).getName(), "Response should carry group name");
    }

    // ── getPermissions (pagination + q) ────────────────────────────
    @Test
    void getPermissions_withBlankQ_usesFindAll() {
        // Arrange
        PaginationRequest pr = new PaginationRequest();
        pr.setOffset(0); pr.setLimit(10); pr.setSortBy("id"); pr.setSortDirection("DESC");
        Page<Permission> page = new PageImpl<>(Collections.emptyList());
        when(permissionRepositoryWrapper.findAll(any(Pageable.class))).thenReturn(page);
        // Act
        PaginatedResponse<PermissionResponse> out = service.getPermissions(pr, " ");
        // Assert
        assertNotNull(out, "getPermissions should return a non-null response");
        verify(permissionRepositoryWrapper).findAll(any(Pageable.class));
    }

    @Test
    void getPermissions_withQ_callsSearch() {
        // Arrange
        PaginationRequest pr = new PaginationRequest();
        pr.setOffset(0); pr.setLimit(10); pr.setSortBy("id"); pr.setSortDirection("DESC");
        Page<Permission> page = new PageImpl<>(Collections.emptyList());
        when(permissionRepositoryWrapper.findByNameContainingIgnoreCase(eq("list"), any(Pageable.class))).thenReturn(page);
        // Act
        PaginatedResponse<PermissionResponse> out = service.getPermissions(pr, " list ");
        // Assert
        assertNotNull(out, "getPermissions should return a non-null response");
        verify(permissionRepositoryWrapper).findByNameContainingIgnoreCase(eq("list"), any(Pageable.class));
    }

    // ── getAllPermissionGroupsWithPermissions ──────────────────────
    @Test
    void getAllPermissionGroupsWithPermissions_buildsGroupWiseLists() {
        // Arrange
        PermissionGroup g = new PermissionGroup(); g.setId(10L); g.setName("Core");
        when(permissionGroupRepositoryWrapper.findAll()).thenReturn(List.of(g));
        com.nivasafinance.features.rolemanagement.mapping.entity.PermissionGroupMapping m =
                new com.nivasafinance.features.rolemanagement.mapping.entity.PermissionGroupMapping();
        m.setPermissionGroupId(10L);
        m.setPermissionId(100L);
        when(permissionGroupMappingRepository.findByPermissionGroupIdIn(List.of(10L)))
                .thenReturn(List.of(m));
        Permission p = new Permission(); p.setId(100L); p.setName("READ");
        when(permissionRepositoryWrapper.findByIdIn(List.of(100L))).thenReturn(List.of(p));
        // Act
        List<PermissionGroupWithPermissionsResponse> out = service.getAllPermissionGroupsWithPermissions();
        // Assert
        assertEquals(1, out.size(), "getAllPermissionGroupsWithPermissions should map groups");
        assertEquals(1, out.get(0).getPermissions().size(), "Each group should list its permissions");
    }

    // ── getAllRoles ────────────────────────────────────────────────
    @Test
    void getAllRoles_mapsResponses() {
        // Arrange
        Role r = new Role(); r.setId(5L); r.setName("Manager");
        when(roleRepositoryWrapper.findAll()).thenReturn(List.of(r));
        // Act
        List<RoleResponse> out = service.getAllRoles();
        // Assert
        assertEquals(1, out.size(), "getAllRoles should map roles");
    }

    // ── getPermissionGroups (pagination + q) ───────────────────────
    @Test
    void getPermissionGroups_withQ_callsSearch() {
        // Arrange
        PaginationRequest pr = new PaginationRequest();
        pr.setOffset(0); pr.setLimit(10); pr.setSortBy("id"); pr.setSortDirection("DESC");
        Page<PermissionGroup> page = new PageImpl<>(Collections.emptyList());
        when(permissionGroupRepositoryWrapper.findByNameContainingIgnoreCase(eq("core"), any(Pageable.class))).thenReturn(page);
        // Act
        PaginatedResponse<PermissionGroupResponse> out = service.getPermissionGroups(pr, " core ");
        // Assert
        assertNotNull(out, "getPermissionGroups should return non-null");
        verify(permissionGroupRepositoryWrapper).findByNameContainingIgnoreCase(eq("core"), any(Pageable.class));
    }

    // ── getRoles (pagination + q) ──────────────────────────────────
    @Test
    void getRoles_withBlankQ_usesFindAll() {
        // Arrange
        PaginationRequest pr = new PaginationRequest();
        pr.setOffset(0); pr.setLimit(10); pr.setSortBy("id"); pr.setSortDirection("DESC");
        Page<Role> page = new PageImpl<>(Collections.emptyList());
        when(roleRepositoryWrapper.findAll(any(Pageable.class))).thenReturn(page);
        // Act
        PaginatedResponse<RoleResponse> out = service.getRoles(pr, " ");
        // Assert
        assertNotNull(out, "getRoles should return non-null");
        verify(roleRepositoryWrapper).findAll(any(Pageable.class));
    }

    // ── getRolePermissions / getRolePermissionGroups ───────────────
    @Test
    void getRolePermissions_delegatesToReadWrapper() {
        // Arrange
        when(permissionReadRepositoryWrapper.findPermissionsByRoleNames(List.of("R1"))).thenReturn(Collections.emptyList());
        // Act
        List<PermissionResponse> out = service.getRolePermissions("R1");
        // Assert
        assertNotNull(out, "getRolePermissions should not return null");
        verify(permissionReadRepositoryWrapper).findPermissionsByRoleNames(List.of("R1"));
    }

    @Test
    void getRolePermissionGroups_mapsGroupIds() {
        // Arrange
        RolePermissionGroupMapping m = new RolePermissionGroupMapping(); m.setPermissionGroupId(10L);
        when(rolePermissionGroupMappingRepository.findByRole("R1")).thenReturn(List.of(m));
        PermissionGroup g = new PermissionGroup(); g.setId(10L); g.setName("Core");
        when(permissionGroupRepositoryWrapper.findById(10L)).thenReturn(g);
        // Act
        List<PermissionGroupResponse> out = service.getRolePermissionGroups("R1");
        // Assert
        assertEquals(1, out.size(), "getRolePermissionGroups should map group ids");
        assertEquals("Core", out.get(0).getName(), "Mapped response should carry group name");
    }

    // ── createRole ─────────────────────────────────────────────────
    @Test
    void createRole_createsWhenNotExists_elseReturnsExisting() {
        // Arrange
        CreateRoleRequest req = new CreateRoleRequest(); req.setName("Ops");
        when(roleRepositoryWrapper.findByNameIgnoreCase("Ops")).thenReturn(Optional.empty());
        when(roleRepositoryWrapper.save(any(Role.class))).thenAnswer(inv -> {
            Role r = inv.getArgument(0); r.setId(99L); return r;
        });
        // Act
        RoleResponse out = service.createRole(req);
        // Assert
        assertEquals("Ops", out.getName(), "createRole should return the role name");
        assertEquals(99L, out.getId(), "createRole should return saved id");
    }

    // ── addPermissionsToRole / removePermissionFromRole ────────────
    @Test
    void addPermissionsToRole_countsAdded_ignoresDuplicates() {
        // Arrange
        UpdateRolePermissionsRequest req = new UpdateRolePermissionsRequest();
        req.setPermissionIds(List.of(1L, 2L));
        doThrow(new DataIntegrityViolationException("dup")).when(rolePermissionMappingRepository).save(argThat(rpm -> rpm.getPermissionId().equals(1L)));
        // Act
        RolePermissionsUpdateResponse out = service.addPermissionsToRole("R", req);
        // Assert
        assertEquals(1, out.getAddedCount(), "addPermissionsToRole should count only successful inserts");
        assertEquals(2, out.getRequestedCount(), "addPermissionsToRole should reflect requested size");
    }

    @Test
    void removePermissionFromRole_delegatesDelete() {
        // Act
        service.removePermissionFromRole("R", 5L);
        // Assert
        verify(rolePermissionMappingRepository).deleteByRoleAndPermissionId("R", 5L);
    }

    // ── addPermissionGroupsToRole / removePermissionGroupFromRole ──
    @Test
    void addPermissionGroupsToRole_countsAdded() {
        // Arrange
        UpdateRolePermissionGroupsRequest req = new UpdateRolePermissionGroupsRequest();
        req.setPermissionGroupIds(List.of(10L, 20L));
        doThrow(new DataIntegrityViolationException("dup")).when(rolePermissionGroupMappingRepository).save(argThat(e -> e.getPermissionGroupId().equals(10L)));
        // Act
        RolePermissionGroupsUpdateResponse out = service.addPermissionGroupsToRole("R", req);
        // Assert
        assertEquals(1, out.getAddedCount(), "addPermissionGroupsToRole should count only successful inserts");
        assertEquals(2, out.getRequestedCount(), "addPermissionGroupsToRole should reflect requested size");
    }

    @Test
    void removePermissionGroupFromRole_delegatesDelete() {
        // Act
        service.removePermissionGroupFromRole("R", 10L);
        // Assert
        verify(rolePermissionGroupMappingRepository).deleteByRoleAndPermissionGroupId("R", 10L);
    }
}

