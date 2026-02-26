package com.nivasafinance.features.rolemanagement.controller;

import com.nivasafinance.common.annotations.RequireRole;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.rolemanagement.admin.service.AdminUserRoleService;
import com.nivasafinance.features.rolemanagement.role.dto.AddUserRolesRequest;
import com.nivasafinance.features.rolemanagement.role.dto.SetPrimaryRoleRequest;
import com.nivasafinance.features.rolemanagement.role.dto.UserRolesResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.V1 + "/admin/users")
@RequiredArgsConstructor
public class AdminUserRoleController {
    private final AdminUserRoleService adminUserRoleService;

    @GetMapping("/{username}/roles")
    @RequireRole({"ADMIN"})
    public ResponseEntity<UserRolesResponse> getUserRoles(@PathVariable String username) {
        return ResponseEntity.ok(adminUserRoleService.getUserRoles(username));
    }

    @PostMapping("/{username}/roles")
    @RequireRole({"ADMIN"})
    public ResponseEntity<UserRolesResponse> addUserRoles(@PathVariable String username,
                                                          @RequestBody AddUserRolesRequest request) {
        return ResponseEntity.ok(adminUserRoleService.addUserRoles(username, request));
    }

    @DeleteMapping("/{username}/roles/{role}")
    @RequireRole({"ADMIN"})
    public ResponseEntity<Void> removeUserRole(@PathVariable String username, @PathVariable String role) {
        adminUserRoleService.removeUserRole(username, role);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{username}/roles/primary")
    @RequireRole({"ADMIN"})
    public ResponseEntity<UserRolesResponse> setPrimaryRole(@PathVariable String username,
                                                            @RequestBody SetPrimaryRoleRequest request) {
        return ResponseEntity.ok(adminUserRoleService.setPrimaryRole(username, request));
    }
}