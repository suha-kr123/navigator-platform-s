package com.nivasafinance.features.usermanagement.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.features.usermanagement.annotation.TransactionalOptimisticRetry;
import com.nivasafinance.features.usermanagement.dto.UserPreferencesRequest;
import com.nivasafinance.features.usermanagement.dto.UserPreferencesResponse;
import com.nivasafinance.features.usermanagement.entity.User;
import com.nivasafinance.features.usermanagement.repository.UserRepository;
import com.nivasafinance.features.usermanagement.service.UserPreferencesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserPreferencesServiceImpl implements UserPreferencesService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserPreferencesResponse getPreferencesForCurrentUser() {
        String username = UserContext.getUsername();
        if (username == null) {
            return UserPreferencesResponse.builder().preferences(new HashMap<>()).build();
        }
        return getPreferencesForUser(username);
    }

    @Override
    @Transactional(readOnly = true)
    public UserPreferencesResponse getPreferencesForUser(String username) {
        return userRepository.findByUsername(username)
                .map(user -> UserPreferencesResponse.builder()
                        .preferences(user.getPreferences() != null ? user.getPreferences() : new HashMap<>())
                        .build())
                .orElse(UserPreferencesResponse.builder().preferences(new HashMap<>()).build());
    }

    @Override
    public UserPreferencesResponse savePreferencesForCurrentUser(UserPreferencesRequest request) {
        String username = UserContext.getUsername();
        if (username == null) {
            throw new IllegalStateException("No current user");
        }
        return savePreferencesForUser(username, request);
    }

    @Override
    public UserPreferencesResponse updatePreferencesForCurrentUser(UserPreferencesRequest request) {
        String username = UserContext.getUsername();
        if (username == null) {
            throw new IllegalStateException("No current user");
        }
        return updatePreferencesForUser(username, request);
    }

    @TransactionalOptimisticRetry
    private UserPreferencesResponse savePreferencesForUser(String username, UserPreferencesRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
        
        user.setPreferences(request.getPreferences() != null ? request.getPreferences() : new HashMap<>());
        User savedUser = userRepository.save(user);
        
        return UserPreferencesResponse.builder()
                .preferences(savedUser.getPreferences() != null ? savedUser.getPreferences() : new HashMap<>())
                .build();
    }

    @TransactionalOptimisticRetry
    private UserPreferencesResponse updatePreferencesForUser(String username, UserPreferencesRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
        
        Map<String, Object> existingPreferences = user.getPreferences() != null 
                ? new HashMap<>(user.getPreferences()) 
                : new HashMap<>();
        
        if (request.getPreferences() != null) {
            existingPreferences.putAll(request.getPreferences());
        }
        
        user.setPreferences(existingPreferences);
        User savedUser = userRepository.save(user);
        
        return UserPreferencesResponse.builder()
                .preferences(savedUser.getPreferences() != null ? savedUser.getPreferences() : new HashMap<>())
                .build();
    }
}

