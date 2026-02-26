package com.nivasafinance.features.rolemanagement.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.common.annotations.RequireRole;
import com.nivasafinance.features.rolemanagement.admin.service.AdminRoleManagementService;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.features.rolemanagement.permission.dto.PermissionResponse;
import com.nivasafinance.features.rolemanagement.permissiongroup.dto.PermissionGroupResponse;
import com.nivasafinance.features.rolemanagement.permissiongroup.dto.PermissionGroupWithPermissionsResponse;
import com.nivasafinance.features.rolemanagement.permissiongroup.dto.UpdateRolePermissionGroupsRequest;
import com.nivasafinance.features.rolemanagement.permission.dto.UpdateRolePermissionsRequest;
import com.nivasafinance.features.rolemanagement.role.dto.CreateRoleRequest;
import com.nivasafinance.features.rolemanagement.role.dto.RoleResponse;
import com.nivasafinance.features.rolemanagement.role.dto.RolePermissionsUpdateResponse;
import com.nivasafinance.features.rolemanagement.role.dto.RolePermissionGroupsUpdateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.V1 + "/admin")
@RequiredArgsConstructor
public class AdminRoleManagementController {

    private final AdminRoleManagementService adminRoleManagementService;

    @GetMapping("/permissions")
    @RequireRole({"ADMIN"})
    public ResponseEntity<PaginatedResponse<PermissionResponse>> getAllPermissions(
            @jakarta.validation.Valid PaginationRequest paginationRequest,
            @RequestParam(value = "q", required = false) String q) {
        String query = q != null ? q.trim() : null;
        if (query != null && !query.isEmpty() && query.length() < 3) {
            PaginatedResponse<PermissionResponse> empty = PaginatedResponse.<PermissionResponse>builder()
                    .content(java.util.Collections.emptyList())
                    .pagination(com.nivasafinance.common.base.model.PaginationInfo.builder()
                            .offset(paginationRequest.getOffset())
                            .limit(paginationRequest.getLimit())
                            .totalElements(0)
                            .totalPages(0)
                            .currentPage(0)
                            .hasNext(false)
                            .hasPrevious(false)
                            .build())
                    .build();
            return ResponseEntity.ok(empty);
        }
        return ResponseEntity.ok(adminRoleManagementService.getPermissions(paginationRequest, query));
    }

    @GetMapping("/permission-groups")
    @RequireRole({"ADMIN"})
    public ResponseEntity<PaginatedResponse<PermissionGroupResponse>> getAllPermissionGroups(
            @jakarta.validation.Valid PaginationRequest paginationRequest,
            @RequestParam(value = "q", required = false) String q) {
        String query = q != null ? q.trim() : null;
        if (query != null && !query.isEmpty() && query.length() < 3) {
            PaginatedResponse<PermissionGroupResponse> empty = PaginatedResponse.<PermissionGroupResponse>builder()
                    .content(java.util.Collections.emptyList())
                    .pagination(com.nivasafinance.common.base.model.PaginationInfo.builder()
                            .offset(paginationRequest.getOffset())
                            .limit(paginationRequest.getLimit())
                            .totalElements(0)
                            .totalPages(0)
                            .currentPage(0)
                            .hasNext(false)
                            .hasPrevious(false)
                            .build())
                    .build();
            return ResponseEntity.ok(empty);
        }
        return ResponseEntity.ok(adminRoleManagementService.getPermissionGroups(paginationRequest, query));
    }
    @GetMapping("/permission-groups/with-permissions")
    @RequireRole({"ADMIN"})
    public ResponseEntity<List<PermissionGroupWithPermissionsResponse>> getAllPermissionGroupsWithPermissions() {
        return ResponseEntity.ok(adminRoleManagementService.getAllPermissionGroupsWithPermissions());
    }

    @GetMapping("/roles")
    @RequireRole({"ADMIN"})
    public ResponseEntity<PaginatedResponse<RoleResponse>> getAllRoles(
            @jakarta.validation.Valid PaginationRequest paginationRequest,
            @RequestParam(value = "q", required = false) String q) {
        String query = q != null ? q.trim() : null;
        if (query != null && !query.isEmpty() && query.length() < 3) {
            PaginatedResponse<RoleResponse> empty = PaginatedResponse.<RoleResponse>builder()
                    .content(java.util.Collections.emptyList())
                    .pagination(com.nivasafinance.common.base.model.PaginationInfo.builder()
                            .offset(paginationRequest.getOffset())
                            .limit(paginationRequest.getLimit())
                            .totalElements(0)
                            .totalPages(0)
                            .currentPage(0)
                            .hasNext(false)
                            .hasPrevious(false)
                            .build())
                    .build();
            return ResponseEntity.ok(empty);
        }
        return ResponseEntity.ok(adminRoleManagementService.getRoles(paginationRequest, query));
    }

    @GetMapping("/roles/{role}/permissions")
    @RequireRole({"ADMIN"})
    public ResponseEntity<List<PermissionResponse>> getRolePermissions(@PathVariable String role) {
        return ResponseEntity.ok(adminRoleManagementService.getRolePermissions(role));
    }

    @GetMapping("/roles/{role}/permission-groups")
    @RequireRole({"ADMIN"})
    public ResponseEntity<List<PermissionGroupResponse>> getRolePermissionGroups(@PathVariable String role) {
        return ResponseEntity.ok(adminRoleManagementService.getRolePermissionGroups(role));
    }

    @PostMapping("/roles")
    @RequireRole({"ADMIN"})
    public ResponseEntity<RoleResponse> createRole(@RequestBody CreateRoleRequest request) {
        return ResponseEntity.ok(adminRoleManagementService.createRole(request));
    }

    @PostMapping("/roles/{role}/permissions")
    @RequireRole({"ADMIN"})
    public ResponseEntity<RolePermissionsUpdateResponse> addPermissionsToRole(@PathVariable String role,
                                                                              @RequestBody UpdateRolePermissionsRequest request) {
        RolePermissionsUpdateResponse resp = adminRoleManagementService.addPermissionsToRole(role, request);
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/roles/{role}/permissions/{permissionId}")
    @RequireRole({"ADMIN"})
    public ResponseEntity<Void> removePermissionFromRole(@PathVariable String role,
                                                         @PathVariable Long permissionId) {
        adminRoleManagementService.removePermissionFromRole(role, permissionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/roles/{role}/permission-groups")
    @RequireRole({"ADMIN"})
    public ResponseEntity<RolePermissionGroupsUpdateResponse> addPermissionGroupsToRole(@PathVariable String role,
                                                                                        @RequestBody UpdateRolePermissionGroupsRequest request) {
        RolePermissionGroupsUpdateResponse resp = adminRoleManagementService.addPermissionGroupsToRole(role, request);
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/roles/{role}/permission-groups/{groupId}")
    @RequireRole({"ADMIN"})
    public ResponseEntity<Void> removePermissionGroupFromRole(@PathVariable String role,
                                                              @PathVariable Long groupId) {
        adminRoleManagementService.removePermissionGroupFromRole(role, groupId);
        return ResponseEntity.noContent().build();
    }
}
