package com.nivasafinance.features.usermanagement.service.impl;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.AddressRequest;
import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.features.person.dto.PersonCreateRequest;
import com.nivasafinance.features.person.dto.PersonCreateResponse;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.dto.PersonUpdateRequest;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.service.PersonReadService;
import com.nivasafinance.features.person.service.PersonWriteService;
import com.nivasafinance.features.usermanagement.dto.UserCreateRequest;
import com.nivasafinance.features.usermanagement.dto.UserResponse;
import com.nivasafinance.features.usermanagement.entity.User;
import com.nivasafinance.features.usermanagement.enums.UserStatus;
import com.nivasafinance.features.usermanagement.exception.UserAlreadyExistsException;
import com.nivasafinance.features.usermanagement.exception.UserNotFoundException;
import com.nivasafinance.features.usermanagement.repository.UserRepository;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserWriteServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PersonWriteService personWriteService;
    @Mock
    private PersonReadService personReadService;
    @Mock
    private UserReadService userReadService;

    @InjectMocks
    private UserWriteServiceImpl service;

    // ── createUser ─────────────────────────────────────────────────
    @Test
    void createUser_missingPerson_throwsBadRequest() {
        // Arrange
        UserCreateRequest request = UserCreateRequest.builder().username("alice").build();

        // Act + Assert
        assertThrows(BadRequestException.class,
                () -> service.createUser(request),
                "createUser should throw BadRequestException when person is null");
    }

    @Test
    void createUser_nullStatus_defaultsToActive() {
        // Arrange
        PersonCreateRequest personReq = PersonCreateRequest.builder().build();
        UserCreateRequest request = UserCreateRequest.builder().username("bob").person(personReq).build();
        when(userRepository.existsByUsername("bob")).thenReturn(false);
        when(personWriteService.createPerson(personReq)).thenReturn(PersonCreateResponse.builder().id(11L).build());

        User saved = new User();
        saved.setId(101L);
        saved.setUsername("bob");
        saved.setStatus(UserStatus.ACTIVE);
        Person personRef = new Person();
        personRef.setId(11L);
        saved.setPerson(personRef);
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(personReadService.getPersonById(11L)).thenReturn(PersonResponse.builder().id(11L).build());

        // Act
        UserResponse response = service.createUser(request);

        // Assert
        assertEquals(UserStatus.ACTIVE, response.getStatus(), "createUser should default status to ACTIVE when null");
    }
    // ── createUser (uniqueness) ────────────────────────────────────
    @Test
    void createUser_usernameExists_throwsUserAlreadyExists() {
        // Arrange
        PersonCreateRequest personReq = PersonCreateRequest.builder().build();
        UserCreateRequest request = UserCreateRequest.builder().username("alice").person(personReq).build();
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        // Act + Assert
        assertThrows(UserAlreadyExistsException.class,
                () -> service.createUser(request),
                "createUser should throw domain exception when username already exists");
        verify(userRepository).existsByUsername("alice");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void createUser_happyPath_persistsAndReturnsResponse() {
        // Arrange
        PersonCreateRequest personReq = PersonCreateRequest.builder().build();
        UserCreateRequest request = UserCreateRequest.builder().username("alice").status(UserStatus.ACTIVE).person(personReq).build();
        PersonCreateResponse personCreateResponse = PersonCreateResponse.builder().id(10L).build();
        when(personWriteService.createPerson(personReq)).thenReturn(personCreateResponse);

        User saved = new User();
        saved.setId(100L);
        saved.setUsername("alice");
        saved.setStatus(UserStatus.ACTIVE);
        Person personRef = new Person();
        personRef.setId(10L);
        saved.setPerson(personRef);
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        PersonResponse personResponse = PersonResponse.builder().id(10L).build();
        when(personReadService.getPersonById(10L)).thenReturn(personResponse);

        // Act
        UserResponse response = service.createUser(request);

        // Assert
        assertEquals(100L, response.getId(), "createUser should return saved user id");
        assertEquals("alice", response.getUsername(), "createUser should return saved username");
        assertEquals(UserStatus.ACTIVE, response.getStatus(), "createUser should return saved status");
        assertEquals(10L, response.getPersonResponse().getId(), "createUser should include person response");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("alice", captor.getValue().getUsername(), "createUser should persist with requested username");
    }

    // ── createUserForExistingPerson ────────────────────────────────
    @Test
    void createUserForExistingPerson_blankUsername_throwsBadRequest() {
        // Arrange
        UserCreateRequest request = UserCreateRequest.builder().username(" ").build();

        // Act + Assert
        assertThrows(BadRequestException.class,
                () -> service.createUserForExistingPerson(request, 15L),
                "createUserForExistingPerson should throw BadRequestException for blank username");
    }

    @Test
    void createUserForExistingPerson_usernameExists_throwsDomainException() {
        // Arrange
        UserCreateRequest request = UserCreateRequest.builder().username("alice").build();
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        // Act + Assert
        assertThrows(UserAlreadyExistsException.class,
                () -> service.createUserForExistingPerson(request, 10L),
                "createUserForExistingPerson should throw domain exception when username exists");
        verify(userRepository).existsByUsername("alice");
    }

    @Test
    void createUserForExistingPerson_happyPath_persistsAndReturnsResponse() {
        // Arrange
        Long personId = 20L;
        UserCreateRequest request = UserCreateRequest.builder().username("carol").status(UserStatus.INACTIVE).build();
        when(userRepository.existsByUsername("carol")).thenReturn(false);
        when(personReadService.getPersonById(personId)).thenReturn(PersonResponse.builder().id(personId).build());

        User saved = new User();
        saved.setId(200L);
        saved.setUsername("carol");
        saved.setStatus(UserStatus.INACTIVE);
        Person personRef = new Person();
        personRef.setId(personId);
        saved.setPerson(personRef);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        // Act
        UserResponse response = service.createUserForExistingPerson(request, personId);

        // Assert
        assertEquals(200L, response.getId(), "createUserForExistingPerson should return saved user id");
        assertEquals(UserStatus.INACTIVE, response.getStatus(), "createUserForExistingPerson should keep provided status");
        assertEquals(personId, response.getPersonResponse().getId(), "createUserForExistingPerson should include person response");
    }

    // ── createUserForMobile ────────────────────────────────────────
    @Test
    void createUserForMobile_nullMobile_throwsBadRequest() {
        // Arrange
        // Act + Assert
        assertThrows(BadRequestException.class,
                () -> service.createUserForMobile(null, PersonCreateRequest.builder().build()),
                "createUserForMobile should throw BadRequestException when mobile is null");
    }

    @Test
    void createUserForMobile_existingPerson_reusesPersonId() {
        // Arrange
        String mobile = "9990001111";
        PersonResponse existing = PersonResponse.builder().id(31L).build();
        when(personReadService.findPersonByPrimaryMobile(mobile)).thenReturn(Optional.of(existing));
        when(userRepository.existsByUsername(mobile)).thenReturn(false);
        User saved = new User();
        saved.setId(310L);
        saved.setUsername(mobile);
        saved.setStatus(UserStatus.ACTIVE);
        Person personRef = new Person();
        personRef.setId(31L);
        saved.setPerson(personRef);
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(personReadService.getPersonById(31L)).thenReturn(existing);

        // Act
        UserResponse response = service.createUserForMobile(mobile, PersonCreateRequest.builder().build());

        // Assert
        assertEquals(mobile, response.getUsername(), "createUserForMobile should create user with mobile as username");
        verify(personWriteService, never()).createPerson(any());
    }

    @Test
    void createUserForMobile_personNotFound_createsPersonFirst() {
        // Arrange
        String mobile = "8887776666";
        when(personReadService.findPersonByPrimaryMobile(mobile)).thenReturn(Optional.empty());
        when(personWriteService.createPerson(any())).thenReturn(PersonCreateResponse.builder().id(41L).build());
        when(userRepository.existsByUsername(mobile)).thenReturn(false);
        User saved = new User();
        saved.setId(410L);
        saved.setUsername(mobile);
        saved.setStatus(UserStatus.ACTIVE);
        Person personRef = new Person();
        personRef.setId(41L);
        saved.setPerson(personRef);
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(personReadService.getPersonById(41L)).thenReturn(PersonResponse.builder().id(41L).build());

        // Act
        UserResponse response = service.createUserForMobile(mobile, PersonCreateRequest.builder().build());

        // Assert
        assertEquals(410L, response.getId(), "createUserForMobile should return saved user id after creating person");
        verify(personWriteService).createPerson(any(PersonCreateRequest.class));
    }

    // ── activateDeactivateUser ─────────────────────────────────────
    @Test
    void activateDeactivateUser_togglesStatus_andFetchesPersonIfPresent() {
        // Arrange
        User existing = new User();
        existing.setId(501L);
        existing.setUsername("dave");
        existing.setStatus(UserStatus.ACTIVE);
        Person person = new Person();
        person.setId(61L);
        existing.setPerson(person);
        when(userRepository.findByUsername("dave")).thenReturn(Optional.of(existing));

        User toggled = new User();
        toggled.setId(501L);
        toggled.setUsername("dave");
        toggled.setStatus(UserStatus.INACTIVE);
        toggled.setPerson(person);
        when(userRepository.save(any(User.class))).thenReturn(toggled);
        when(personReadService.getPersonById(61L)).thenReturn(PersonResponse.builder().id(61L).build());

        // Act
        UserResponse response = service.activateDeactivateUser("dave");

        // Assert
        assertEquals(UserStatus.INACTIVE, response.getStatus(), "activateDeactivateUser should toggle status");
        verify(personReadService).getPersonById(61L);
    }

    @Test
    void activateDeactivateUser_withoutPerson_doesNotFetchPerson() {
        // Arrange
        User existing = new User();
        existing.setId(502L);
        existing.setUsername("erin");
        existing.setStatus(UserStatus.INACTIVE);
        when(userRepository.findByUsername("erin")).thenReturn(Optional.of(existing));

        User toggled = new User();
        toggled.setId(502L);
        toggled.setUsername("erin");
        toggled.setStatus(UserStatus.ACTIVE);
        when(userRepository.save(any(User.class))).thenReturn(toggled);

        // Act
        UserResponse response = service.activateDeactivateUser("erin");

        // Assert
        assertEquals(UserStatus.ACTIVE, response.getStatus(), "activateDeactivateUser should toggle to ACTIVE");
        verify(personReadService, never()).getPersonById(anyLong());
    }

    @Test
    void activateDeactivateUser_userNotFound_throwsUserNotFoundException() {
        // Arrange
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(UserNotFoundException.class,
                () -> service.activateDeactivateUser("missing"),
                "activateDeactivateUser should throw UserNotFoundException when user does not exist");
        verify(userRepository).findByUsername("missing");
        verify(userRepository, never()).save(any());
    }

    // ── person + address helpers ───────────────────────────────────
    @Test
    void updatePersonForUser_delegatesToPersonWriteService() {
        // Arrange
        when(userReadService.getPersonIdByUsername("frank")).thenReturn(71L);
        PersonUpdateRequest req = PersonUpdateRequest.builder().build();

        // Act
        service.updatePersonForUser("frank", req);

        // Assert
        verify(personWriteService).updatePerson(71L, req);
    }

    @Test
    void addAddressForUser_delegatesAndReturnsId() {
        // Arrange
        when(userReadService.getPersonIdByUsername("gina")).thenReturn(81L);
        AddressRequest req = new AddressRequest();
        when(personWriteService.addAddress(81L, req)).thenReturn("ADDR-1");

        // Act
        String id = service.addAddressForUser("gina", req);

        // Assert
        assertEquals("ADDR-1", id, "addAddressForUser should return address id from person service");
        verify(personWriteService).addAddress(81L, req);
    }

    @Test
    void updateAddressForUser_delegatesAndReturnsData() {
        // Arrange
        when(userReadService.getPersonIdByUsername("helen")).thenReturn(91L);
        AddressRequest req = new AddressRequest();
        AddressData data = AddressData.builder().id("A-2").build();
        when(personWriteService.updateAddress(91L, "A-2", req)).thenReturn(data);

        // Act
        AddressData result = service.updateAddressForUser("helen", "A-2", req);

        // Assert
        assertEquals("A-2", result.getId(), "updateAddressForUser should return updated address data");
        verify(personWriteService).updateAddress(91L, "A-2", req);
    }
}

