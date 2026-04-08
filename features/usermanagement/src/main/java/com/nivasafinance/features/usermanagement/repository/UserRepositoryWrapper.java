package com.nivasafinance.features.usermanagement.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.usermanagement.dto.UserResponse;
import com.nivasafinance.features.usermanagement.entity.User;
import com.nivasafinance.features.usermanagement.exception.UserExceptionFactory;
import com.nivasafinance.features.usermanagement.enums.UserStatus;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserRepositoryWrapper {

    private static final String DELETED_USERS_SELECT = """
            SELECT u.id AS user_id,
                   u.username,
                   u.status,
                   u.is_deleted AS user_deleted,
                   p.id AS person_id,
                   p.first_name,
                   p.middle_name,
                   p.last_name,
                   p.display_name,
                   p.email,
                   p.mobile_numbers
            FROM n_user u
            LEFT JOIN n_person p ON p.id = u.person_id AND p.is_deleted = false
            WHERE u.is_deleted = true
            ORDER BY u.updated_at DESC
            LIMIT :limit OFFSET :offset
            """;

    private static final String DELETED_USERS_COUNT = """
            SELECT COUNT(*)
            FROM n_user u
            WHERE u.is_deleted = true
            """;

    private final UserRepository userRepository;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public User findByIdWithException(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> UserExceptionFactory.userNotFoundById(userId));
    }

    public User findByUsernameWithException(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> UserExceptionFactory.userNotFoundByUsername(username));
    }

    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByPersonId(Long personId) {
        return userRepository.findByPerson_Id(personId);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public List<User> findByPersonPhoneNumber(String phoneNumber) {
        return userRepository.findByPersonPhoneNumber(phoneNumber);
    }

    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable) {
        return userRepository.findByUsernameContainingIgnoreCase(username, pageable);
    }

    public Page<User> findByIsDeletedTrue(Pageable pageable) {
        return userRepository.findByIsDeletedTrue(pageable);
    }

    public PaginatedResponse<UserResponse> findDeletedUsersWithLightweightPerson(PaginationRequest paginationRequest) {
        int offset = paginationRequest.getOffset();
        int limit = paginationRequest.getLimit();

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("limit", limit)
                .addValue("offset", offset);

        try {
            List<UserResponse> users = jdbcTemplate.query(DELETED_USERS_SELECT, params, (rs, rowNum) ->
                    UserResponse.builder()
                            .id(rs.getLong("user_id"))
                            .personResponse(buildPersonSummary(
                                    rs.getObject("person_id", Long.class),
                                    rs.getString("first_name"),
                                    rs.getString("middle_name"),
                                    rs.getString("last_name"),
                                    rs.getString("display_name"),
                                    rs.getString("email"),
                                    rs.getString("mobile_numbers")
                            ))
                            .username(rs.getString("username"))
                            .status(readUserStatus(rs.getString("status")))
                            .deleted(rs.getBoolean("user_deleted"))
                            .build()
            );

            Long totalCount = jdbcTemplate.queryForObject(DELETED_USERS_COUNT, new MapSqlParameterSource(), Long.class);
            long totalElements = totalCount != null ? totalCount : 0L;
            int totalPages = limit == 0 ? 0 : (int) Math.ceil(totalElements / (double) limit);
            int currentPage = limit == 0 ? 0 : offset / limit;

            return new PaginatedResponse<>(users, new PaginationInfo(
                    offset,
                    limit,
                    totalElements,
                    totalPages,
                    currentPage,
                    offset + limit < totalElements,
                    offset > 0
            ));
        } catch (DataAccessException ex) {
            throw UserExceptionFactory.userOperationFailed("retrieve deleted users", ex);
        }
    }

    private PersonResponse buildPersonSummary(Long personId,
                                              String firstName,
                                              String middleName,
                                              String lastName,
                                              String displayName,
                                              String email,
                                              String mobileNumbersJson) {
        if (personId == null) {
            return null;
        }

        return PersonResponse.builder()
                .id(personId)
                .firstName(firstName)
                .middleName(middleName)
                .lastName(lastName)
                .displayName(displayName)
                .email(email)
                .mobileNumbers(readMobileNumbers(mobileNumbersJson))
                .build();
    }

    private List<MobileNumberDetails> readMobileNumbers(String mobileNumbersJson) {
        if (mobileNumbersJson == null || mobileNumbersJson.isBlank()) {
            return Collections.emptyList();
        }

        try {
            return objectMapper.readValue(mobileNumbersJson, new TypeReference<List<MobileNumberDetails>>() {
            });
        } catch (Exception ex) {
            throw UserExceptionFactory.userOperationFailed("parse deleted user mobile numbers", ex);
        }
    }

    private UserStatus readUserStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return UserStatus.valueOf(status);
    }
}
