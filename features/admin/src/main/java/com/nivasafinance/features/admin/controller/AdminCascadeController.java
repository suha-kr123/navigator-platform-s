package com.nivasafinance.features.admin.controller;

import com.nivasafinance.common.annotations.RequireRole;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.admin.service.AdminCascadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.V1 + "/admin/cascade")
@RequiredArgsConstructor
public class AdminCascadeController {

    private final AdminCascadeService adminCascadeService;

    @PostMapping("/person/{mobileNumber}/delete")
    @RequireRole({"ADMIN"})
    public ResponseEntity<Void> cascadeDeletePerson(@PathVariable String mobileNumber) {
        adminCascadeService.cascadeDeletePerson(mobileNumber);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/person/{mobileNumber}/undo-delete")
    @RequireRole({"ADMIN"})
    public ResponseEntity<Void> cascadeRestorePerson(@PathVariable String mobileNumber) {
        adminCascadeService.cascadeRestorePerson(mobileNumber);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/user/{username}/delete")
    @RequireRole({"ADMIN"})
    public ResponseEntity<Void> cascadeDeleteUser(@PathVariable String username) {
        adminCascadeService.cascadeDeleteUser(username);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/user/{username}/undo-delete")
    @RequireRole({"ADMIN"})
    public ResponseEntity<Void> cascadeRestoreUser(@PathVariable String username) {
        adminCascadeService.cascadeRestoreUser(username);
        return ResponseEntity.noContent().build();
    }
}
