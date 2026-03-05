package com.nivasafinance.features.usermanagement.controller;

import com.nivasafinance.common.annotations.RequireRole;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.features.usermanagement.dto.UserResponse;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import com.nivasafinance.features.usermanagement.service.UserWriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.V1 + "/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserReadService userReadService;
    private final UserWriteService userWriteService;

    @PostMapping("/{username}/activate-deactivate")
    @RequireRole({"ADMIN"})
    public ResponseEntity<UserResponse> activateDeactivateUser(@PathVariable String username) {
        UserResponse response = userWriteService.activateDeactivateUser(username);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @RequireRole({"ADMIN"})
    public ResponseEntity<PaginatedResponse<UserResponse>> getUsers(
            @Valid PaginationRequest paginationRequest,
            @RequestParam(value = "q", required = false) String q) {
        String query = q != null ? q.trim() : null;
        if (query != null && !query.isEmpty() && query.length() < 3) {
            PaginatedResponse<UserResponse> empty = PaginatedResponse.<UserResponse>builder()
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
        return ResponseEntity.ok(userReadService.getUsers(paginationRequest, query));
    }
}
