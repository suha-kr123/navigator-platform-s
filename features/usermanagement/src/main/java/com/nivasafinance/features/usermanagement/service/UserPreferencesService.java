package com.nivasafinance.features.usermanagement.service;

import com.nivasafinance.features.usermanagement.dto.UserPreferencesRequest;
import com.nivasafinance.features.usermanagement.dto.UserPreferencesResponse;

public interface UserPreferencesService {
    UserPreferencesResponse getPreferencesForCurrentUser();
    UserPreferencesResponse getPreferencesForUser(String username);
    UserPreferencesResponse savePreferencesForCurrentUser(UserPreferencesRequest request);
    UserPreferencesResponse updatePreferencesForCurrentUser(UserPreferencesRequest request);
}

