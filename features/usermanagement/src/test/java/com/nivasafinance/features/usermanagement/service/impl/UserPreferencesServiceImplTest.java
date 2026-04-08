package com.nivasafinance.features.usermanagement.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.features.usermanagement.dto.UserPreferencesRequest;
import com.nivasafinance.features.usermanagement.dto.UserPreferencesResponse;
import com.nivasafinance.features.usermanagement.entity.User;
import com.nivasafinance.features.usermanagement.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPreferencesServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserPreferencesServiceImpl service;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    // ── getPreferencesForCurrentUser ───────────────────────────────
    @Test
    void getPreferencesForCurrentUser_noUsername_returnsEmptyPreferences() {
        // Arrange
        UserContext.clear();

        // Act
        UserPreferencesResponse response = service.getPreferencesForCurrentUser();

        // Assert
        assertNotNull(response, "getPreferencesForCurrentUser should return a non-null response");
        assertNotNull(response.getPreferences(), "getPreferencesForCurrentUser should return non-null preferences map");
        assertTrue(response.getPreferences().isEmpty(), "getPreferencesForCurrentUser should return empty preferences when no user");
        verifyNoInteractions(userRepository);
    }

    // ── getPreferencesForUser ──────────────────────────────────────
    @Test
    void getPreferencesForUser_userNotFound_returnsEmptyPreferences() {
        // Arrange
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());

        // Act
        UserPreferencesResponse response = service.getPreferencesForUser("alice");

        // Assert
        assertNotNull(response.getPreferences(), "getPreferencesForUser should return non-null preferences when user not found");
        assertTrue(response.getPreferences().isEmpty(), "getPreferencesForUser should return empty preferences when user not found");
    }

    @Test
    void getPreferencesForUser_userHasNullPreferences_returnsEmptyPreferences() {
        // Arrange
        User user = new User();
        user.setPreferences(null);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        // Act
        UserPreferencesResponse response = service.getPreferencesForUser("alice");

        // Assert
        assertNotNull(response.getPreferences(), "getPreferencesForUser should return non-null preferences when stored preferences are null");
        assertTrue(response.getPreferences().isEmpty(), "getPreferencesForUser should return empty preferences when stored preferences are null");
    }

    // ── savePreferencesForCurrentUser ──────────────────────────────
    @Test
    void savePreferencesForCurrentUser_noUsername_throwsIllegalState() {
        // Arrange
        UserContext.clear();

        // Act + Assert
        assertThrows(IllegalStateException.class,
                () -> service.savePreferencesForCurrentUser(UserPreferencesRequest.builder().build()),
                "savePreferencesForCurrentUser should throw when there is no current user");
    }

    @Test
    void savePreferencesForCurrentUser_userNotFound_throwsIllegalState() {
        // Arrange
        UserContext.setUsername("missing");
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(IllegalStateException.class,
                () -> service.savePreferencesForCurrentUser(UserPreferencesRequest.builder().build()),
                "savePreferencesForCurrentUser should throw when user does not exist");
    }

    @Test
    void savePreferencesForCurrentUser_nullRequestPreferences_savesEmptyMap() {
        // Arrange
        UserContext.setUsername("alice");
        User user = new User();
        user.setPreferences(null);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        UserPreferencesResponse response = service.savePreferencesForCurrentUser(UserPreferencesRequest.builder().preferences(null).build());

        // Assert
        assertNotNull(response.getPreferences(), "savePreferencesForCurrentUser should return non-null preferences");
        assertTrue(response.getPreferences().isEmpty(), "savePreferencesForCurrentUser should default to empty map when request preferences are null");
        verify(userRepository).save(any(User.class));
    }

    // ── updatePreferencesForCurrentUser ────────────────────────────
    @Test
    void updatePreferencesForCurrentUser_noUsername_throwsIllegalState() {
        // Arrange
        UserContext.clear();

        // Act + Assert
        assertThrows(IllegalStateException.class,
                () -> service.updatePreferencesForCurrentUser(UserPreferencesRequest.builder().build()),
                "updatePreferencesForCurrentUser should throw when there is no current user");
    }

    @Test
    void updatePreferencesForCurrentUser_mergesIntoExistingPreferences() {
        // Arrange
        UserContext.setUsername("alice");
        User user = new User();
        Map<String, Object> existing = new HashMap<>();
        existing.put("theme", "dark");
        user.setPreferences(existing);
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> update = new HashMap<>();
        update.put("lang", "en");
        UserPreferencesRequest request = UserPreferencesRequest.builder().preferences(update).build();

        // Act
        UserPreferencesResponse response = service.updatePreferencesForCurrentUser(request);

        // Assert
        assertTrue(response.getPreferences().containsKey("theme"),
                "updatePreferencesForCurrentUser should keep existing keys when merging");
        assertTrue(response.getPreferences().containsKey("lang"),
                "updatePreferencesForCurrentUser should add new keys when merging");
        verify(userRepository).save(any(User.class));
    }
}

