package com.nivasafinance.features.admin.controller;

import com.nivasafinance.common.annotations.RequireRole;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.admin.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.V1 + "/admin/users")
@RequiredArgsConstructor
public class AdminUserSoftDeleteController {

    private final AdminUserService adminUserService;

    @PostMapping("/{username}/delete")
    @RequireRole({"ADMIN"})
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        adminUserService.deleteUser(username);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{username}/undo-delete")
    @RequireRole({"ADMIN"})
    public ResponseEntity<Void> undoDeleteUser(@PathVariable String username) {
        adminUserService.undoDeleteUser(username);
        return ResponseEntity.noContent().build();
    }
}
