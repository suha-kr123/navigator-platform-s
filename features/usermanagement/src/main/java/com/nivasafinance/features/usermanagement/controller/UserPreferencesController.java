package com.nivasafinance.features.usermanagement.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.usermanagement.dto.UserPreferencesRequest;
import com.nivasafinance.features.usermanagement.dto.UserPreferencesResponse;
import com.nivasafinance.features.usermanagement.service.UserPreferencesService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiConstants.V1 + "/users/preferences")
@AllArgsConstructor
public class UserPreferencesController {

    private final UserPreferencesService userPreferencesService;

    @GetMapping
    public ResponseEntity<UserPreferencesResponse> getPreferences() {
        UserPreferencesResponse response = userPreferencesService.getPreferencesForCurrentUser();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<UserPreferencesResponse> savePreferences(
            @Valid @RequestBody UserPreferencesRequest request) {
        UserPreferencesResponse response = userPreferencesService.savePreferencesForCurrentUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping
    public ResponseEntity<UserPreferencesResponse> updatePreferences(
            @Valid @RequestBody UserPreferencesRequest request) {
        UserPreferencesResponse response = userPreferencesService.updatePreferencesForCurrentUser(request);
        return ResponseEntity.ok(response);
    }
}

