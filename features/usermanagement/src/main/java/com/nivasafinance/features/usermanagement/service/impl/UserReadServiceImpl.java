package com.nivasafinance.features.usermanagement.service.impl;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.service.PersonReadService;
import com.nivasafinance.features.usermanagement.dto.UserResponse;
import com.nivasafinance.features.usermanagement.entity.User;
import com.nivasafinance.features.usermanagement.exception.UserExceptionFactory;
import com.nivasafinance.features.usermanagement.repository.UserRepositoryWrapper;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Objects;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class UserReadServiceImpl implements UserReadService {

    private final UserRepositoryWrapper userRepositoryWrapper;
    private final PersonReadService personReadService;

    @Override
    public Optional<UserResponse> findUserByPersonMobile(String mobile) {
        return userRepositoryWrapper.findByPersonPhoneNumber(mobile).stream()
                .filter(u -> u.getPerson() != null)
                .findFirst()
                .map(this::mapToResponse);
    }

    @Override
    public Optional<String> resolveUsernameByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return personReadService.findPersonByEmail(email)
                .map(PersonResponse::getId)
                .flatMap(userRepositoryWrapper::findByPersonId)
                .map(User::getUsername);
    }

    @Override
    public Optional<String> resolveUsernameByPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return Optional.empty();
        }
        return findUserByPersonMobile(phone).map(UserResponse::getUsername);
    }

    @Override
    public UserResponse getUserById(Long userId) {
        User user = userRepositoryWrapper.findByIdWithException(userId);
        return mapToResponse(user);
    }

    @Override
    public UserResponse getUserByUsername(String username) {
        User user = userRepositoryWrapper.findByUsernameWithException(username);
        return mapToResponse(user);
    }

    @Override
    public void checkForUserNameAvailability(String username) {
        if(userRepositoryWrapper.existsByUsername(username)){
            throw new BadRequestException("Username is already taken");
        }
    }

    @Override
    public Optional<User> findUserByUsername(String username) {
        return userRepositoryWrapper.findByUsername(username);
    }

    @Override
    public List<User> findUsersByPersonPhoneNumber(String phoneNumber) {
        return userRepositoryWrapper.findByPersonPhoneNumber(phoneNumber);
    }

    @Override
    public PaginatedResponse<UserResponse> getUsers(PaginationRequest pagination, String q) {
        int limit = Math.max(1, pagination.getLimit());
        int pageNumber = Math.max(0, pagination.getOffset() / limit);
        Sort.Direction dir = Sort.Direction.fromOptionalString(pagination.getSortDirection()).orElse(Sort.Direction.DESC);
        Sort sort = Sort.by(dir, pagination.getSortBy());
        Pageable pageable = PageRequest.of(pageNumber, limit, sort);
        Page<User> page;
        if (q != null && !q.isBlank()) {
            page = userRepositoryWrapper.findByUsernameContainingIgnoreCase(q.trim(), pageable);
        } else {
            page = userRepositoryWrapper.findAll(pageable);
        }
        List<UserResponse> content = page.getContent().stream().map(this::mapToResponse).filter(Objects::nonNull).collect(Collectors.toList());
        PaginationInfo info = PaginationInfo.builder()
                .offset(pagination.getOffset())
                .limit(limit)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .currentPage(page.getNumber())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
        return PaginatedResponse.<UserResponse>builder()
                .content(content)
                .pagination(info)
                .build();
    }
    @Override
    public Long getPersonIdByUsername(String username) {
        UserResponse user = getUserByUsername(username);
        if (user.getPersonResponse() == null || user.getPersonResponse().getId() == null) {
            throw new BadRequestException("User has no linked person: " + username);
        }
        return user.getPersonResponse().getId();
    }



    @Override
    public PersonResponse getPersonForUser(String username) {
        Long personId = getPersonIdByUsername(username);
        return personReadService.getPersonById(personId);
    }



    @Override
    public List<AddressData> getAddressesForUser(String username) {
        Long personId = getPersonIdByUsername(username);
        return personReadService.getAddresses(personId);
    }


    @Override
    public AddressData getAddressForUser(String username, String addressId) {
        Long personId = getPersonIdByUsername(username);
        return personReadService.getAddress(personId, addressId);
    }

    private UserResponse mapToResponse(User user) {
        // Fetch PersonResponse if person is linked
        PersonResponse personResponse = null;
        if (user.getPerson() != null) {
            personResponse = personReadService.getPersonById(user.getPerson().getId());
        }

        return UserResponse.builder()
                .id(user.getId())
                .personResponse(personResponse)  // Set full PersonResponse
                .username(user.getUsername())
                .status(user.getStatus())
                .deleted(user.getIsDeleted())
                .build();
    }

    @Override
    public UserResponse adminGetUserByUsername(String username) {
        User user = userRepositoryWrapper.findByUsername(username)
                .orElseThrow(() -> UserExceptionFactory.userNotFoundByUsername(username));
        try {
            return mapToResponse(user);
        } catch (ResourceNotFoundException e) {
            // Person may also be soft-deleted — return user without person details
            return UserResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .status(user.getStatus())
                    .deleted(user.getIsDeleted())
                    .build();
        }
    }

    @Override
    public UserResponse adminGetUserById(Long userId) {
        User user = userRepositoryWrapper.findByIdWithException(userId);
        try {
            return mapToResponse(user);
        } catch (ResourceNotFoundException e) {
            return UserResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .status(user.getStatus())
                    .deleted(user.getIsDeleted())
                    .build();
        }
    }

    @Override
    public PaginatedResponse<UserResponse> getDeletedUsers(PaginationRequest pagination) {
        return userRepositoryWrapper.findDeletedUsersWithLightweightPerson(pagination);
    }
}
