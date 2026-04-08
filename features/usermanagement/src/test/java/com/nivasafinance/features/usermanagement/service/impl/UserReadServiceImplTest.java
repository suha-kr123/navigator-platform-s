package com.nivasafinance.features.usermanagement.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.service.PersonReadService;
import com.nivasafinance.features.usermanagement.dto.UserResponse;
import com.nivasafinance.features.usermanagement.entity.User;
import com.nivasafinance.features.usermanagement.enums.UserStatus;
import com.nivasafinance.features.usermanagement.repository.UserRepositoryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserReadServiceImplTest {

    @Mock
    private UserRepositoryWrapper userRepositoryWrapper;
    @Mock
    private PersonReadService personReadService;

    @InjectMocks
    private UserReadServiceImpl service;

    // ── resolveUsernameByEmail ─────────────────────────────────────
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.NullAndEmptySource
    void resolveUsernameByEmail_nullOrBlank_returnsEmpty(String email) {
        // Act
        Optional<String> result = service.resolveUsernameByEmail(email);
        // Assert
        assertTrue(result.isEmpty(), "resolveUsernameByEmail should return empty for null or blank email");
        verifyNoInteractions(personReadService, userRepositoryWrapper);
    }

    // ── resolveUsernameByEmail (present flow) ──────────────────────
    @Test
    void resolveUsernameByEmail_personFound_userFound_returnsUsername() {
        // Arrange
        when(personReadService.findPersonByEmail("a@b.com"))
                .thenReturn(Optional.of(PersonResponse.builder().id(10L).build()));
        User user = new User();
        user.setUsername("alice");
        when(userRepositoryWrapper.findByPersonId(10L)).thenReturn(Optional.of(user));

        // Act
        Optional<String> result = service.resolveUsernameByEmail("a@b.com");

        // Assert
        assertTrue(result.isPresent(), "resolveUsernameByEmail should return username when mapping exists");
        assertEquals("alice", result.get(), "resolveUsernameByEmail should return mapped username");
        verify(userRepositoryWrapper).findByPersonId(10L);
    }

    // ── resolveUsernameByPhone ─────────────────────────────────────
    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.NullAndEmptySource
    void resolveUsernameByPhone_nullOrBlank_returnsEmpty(String phone) {
        // Act
        Optional<String> result = service.resolveUsernameByPhone(phone);
        // Assert
        assertTrue(result.isEmpty(), "resolveUsernameByPhone should return empty for null or blank phone");
        verifyNoInteractions(userRepositoryWrapper, personReadService);
    }

    // ── findUserByPersonMobile ─────────────────────────────────────
    @Test
    void findUserByPersonMobile_userListHasNoPerson_returnsEmpty() {
        // Arrange
        User u = new User();
        u.setUsername("u1");
        u.setPerson(null);
        when(userRepositoryWrapper.findByPersonPhoneNumber("999")).thenReturn(List.of(u));

        // Act
        Optional<UserResponse> result = service.findUserByPersonMobile("999");

        // Assert
        assertTrue(result.isEmpty(), "findUserByPersonMobile should return empty when no user has linked person");
        verify(personReadService, never()).getPersonById(anyLong());
    }

    // ── getUserByUsername ──────────────────────────────────────────
    @Test
    void getUserByUsername_personLinked_includesPersonResponse() {
        // Arrange
        User u = new User();
        u.setId(1L);
        u.setUsername("alice");
        u.setStatus(UserStatus.ACTIVE);
        com.nivasafinance.features.person.entity.Person p = new com.nivasafinance.features.person.entity.Person();
        p.setId(20L);
        u.setPerson(p);
        when(userRepositoryWrapper.findByUsernameWithException("alice")).thenReturn(u);
        when(personReadService.getPersonById(20L)).thenReturn(PersonResponse.builder().id(20L).build());

        // Act
        UserResponse response = service.getUserByUsername("alice");

        // Assert
        assertEquals(20L, response.getPersonResponse().getId(), "getUserByUsername should include personResponse when user has person");
        verify(personReadService).getPersonById(20L);
    }

    // ── checkForUserNameAvailability ───────────────────────────────
    @Test
    void checkForUserNameAvailability_usernameExists_throwsBadRequest() {
        // Arrange
        when(userRepositoryWrapper.existsByUsername("taken")).thenReturn(true);

        // Act + Assert
        assertThrows(BadRequestException.class,
                () -> service.checkForUserNameAvailability("taken"),
                "checkForUserNameAvailability should throw when username already exists");
        verify(userRepositoryWrapper).existsByUsername("taken");
    }

    // ── getPersonIdByUsername ──────────────────────────────────────
    @Test
    void getPersonIdByUsername_personMissing_throwsBadRequest() {
        // Arrange
        UserResponse userResponse = UserResponse.builder().username("u").personResponse(null).build();
        UserReadServiceImpl spy = spy(service);
        doReturn(userResponse).when(spy).getUserByUsername("u");

        // Act + Assert
        assertThrows(BadRequestException.class,
                () -> spy.getPersonIdByUsername("u"),
                "getPersonIdByUsername should throw when personResponse is missing");
    }

    // ── getUsers (pagination + query) ──────────────────────────────
    @Test
    void getUsers_qBlank_usesFindAll() {
        // Arrange
        PaginationRequest pagination = new PaginationRequest();
        pagination.setOffset(0);
        pagination.setLimit(10);
        pagination.setSortBy("id");
        pagination.setSortDirection("DESC");
        Page<User> emptyPage = new PageImpl<>(Collections.emptyList());
        when(userRepositoryWrapper.findAll(any(Pageable.class))).thenReturn(emptyPage);

        // Act
        PaginatedResponse<UserResponse> response = service.getUsers(pagination, "  ");

        // Assert
        assertNotNull(response, "getUsers should return non-null response");
        verify(userRepositoryWrapper).findAll(any(Pageable.class));
        verify(userRepositoryWrapper, never()).findByUsernameContainingIgnoreCase(anyString(), any(Pageable.class));
    }

    @Test
    void getUsers_qProvided_usesSearchQueryTrimmed() {
        // Arrange
        PaginationRequest pagination = new PaginationRequest();
        pagination.setOffset(0);
        pagination.setLimit(10);
        pagination.setSortBy("id");
        pagination.setSortDirection("DESC");
        Page<User> emptyPage = new PageImpl<>(Collections.emptyList());
        when(userRepositoryWrapper.findByUsernameContainingIgnoreCase(eq("bob"), any(Pageable.class))).thenReturn(emptyPage);

        // Act
        PaginatedResponse<UserResponse> response = service.getUsers(pagination, "  bob  ");

        // Assert
        assertNotNull(response, "getUsers should return non-null response");
        verify(userRepositoryWrapper).findByUsernameContainingIgnoreCase(eq("bob"), any(Pageable.class));
        verify(userRepositoryWrapper, never()).findAll(any(Pageable.class));
    }

    // ── address helpers ────────────────────────────────────────────
    @Test
    void getAddressesForUser_delegatesToPersonReadService() {
        // Arrange
        UserResponse userResponse = UserResponse.builder()
                .username("alice")
                .personResponse(PersonResponse.builder().id(55L).build())
                .build();
        UserReadServiceImpl spy = spy(service);
        doReturn(userResponse).when(spy).getUserByUsername("alice");
        List<AddressData> addresses = Collections.emptyList();
        when(personReadService.getAddresses(55L)).thenReturn(addresses);

        // Act
        List<AddressData> result = spy.getAddressesForUser("alice");

        // Assert
        assertSame(addresses, result, "getAddressesForUser should return the same list from personReadService");
        verify(personReadService).getAddresses(55L);
    }
}

