package com.nivasafinance.features.admin.controller;

import com.nivasafinance.common.annotations.RequireRole;
import com.nivasafinance.features.admin.dto.MobileSearchRequest;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.admin.service.AdminPersonService;
import com.nivasafinance.features.person.dto.AdminPersonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.V1 + "/admin/persons")
@RequiredArgsConstructor
public class AdminPersonController {

    private final AdminPersonService adminPersonService;

    @PostMapping("/{mobileNumber}/delete")
    @RequireRole({"ADMIN"})
    public ResponseEntity<Void> deletePerson(@PathVariable String mobileNumber) {
        adminPersonService.deletePerson(mobileNumber);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{mobileNumber}/undo-delete")
    @RequireRole({"ADMIN"})
    public ResponseEntity<Void> undoDeletePerson(@PathVariable String mobileNumber) {
        adminPersonService.undoDeletePerson(mobileNumber);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/search")
    @RequireRole({"ADMIN"})
    public ResponseEntity<PaginatedResponse<AdminPersonResponse>> adminSearchPersons(
            @Valid PaginationRequest paginationRequest,
            @Valid @RequestBody MobileSearchRequest request
    ) {
        return ResponseEntity.ok(adminPersonService.adminSearchPersonsByMobile(paginationRequest, request.getMobileNumber().trim()));
    }

    @GetMapping("/deleted")
    @RequireRole({"ADMIN"})
    public ResponseEntity<PaginatedResponse<AdminPersonResponse>> getDeletedPersons(
            @Valid PaginationRequest paginationRequest
    ) {
        return ResponseEntity.ok(adminPersonService.getDeletedPersons(paginationRequest));
    }
}
