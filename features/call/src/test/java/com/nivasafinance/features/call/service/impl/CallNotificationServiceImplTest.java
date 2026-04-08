package com.nivasafinance.features.call.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.dto.CallNotificationResponse;
import com.nivasafinance.features.call.entity.CallLog;
import com.nivasafinance.features.call.enums.CallDirection;
import com.nivasafinance.features.call.enums.CallStatus;
import com.nivasafinance.features.call.repository.CallNotificationRedisRepository;
import com.nivasafinance.features.call.service.CallNotificationSseService;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.service.PersonReadService;
import com.nivasafinance.features.usermanagement.entity.User;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CallNotificationServiceImplTest {

    @Mock
    private UserReadService userReadService;
    @Mock
    private PersonReadService personReadService;
    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private CallNotificationRedisRepository redisRepository;
    @Mock
    private CallNotificationSseService sseService;

    @InjectMocks
    private CallNotificationServiceImpl service;

    @Test
    void getNotificationsForCurrentUser_noUsername_returnsEmpty() {
        PaginationRequest request = new PaginationRequest(0, 5, "createdAt", "DESC");

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn(null);

            PaginatedResponse<?> response = service.getNotificationsForCurrentUser(request);

            assertNotNull(response);
            assertTrue(response.getContent().isEmpty());
            assertNotNull(response.getPagination());
            assertEquals(0, response.getPagination().getTotalElements());
        }
    }

    @Test
    void getNotificationsForCurrentUser_userWithoutPerson_returnsEmpty() {
        PaginationRequest request = new PaginationRequest(0, 5, "createdAt", "DESC");
        User user = new User();

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("alice");
            when(userReadService.findUserByUsername("alice")).thenReturn(Optional.of(user));

            PaginatedResponse<?> response = service.getNotificationsForCurrentUser(request);

            assertNotNull(response);
            assertTrue(response.getContent().isEmpty());
        }
    }


    @Test
    void getRecentNotificationsFromRedis_noUsername_returnsEmptyOptional() {
        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn(null);

            Optional<CallNotificationResponse> result = service.getRecentNotificationsFromRedis();

            assertTrue(result.isEmpty());
        }
    }

    @Test
    void getRecentNotificationsFromRedis_returnsLatest() {
        User user = new User();
        Person person = new Person();
        person.setMobileNumbers(List.of(MobileNumberDetails.builder()
                .number("9876543210")
                .isPrimary(true)
                .build()));
        user.setPerson(person);

        CallNotificationResponse older = CallNotificationResponse.builder()
                .callSid("old")
                .createdAt(LocalDateTime.now().minusMinutes(5))
                .build();
        CallNotificationResponse newer = CallNotificationResponse.builder()
                .callSid("new")
                .createdAt(LocalDateTime.now())
                .build();

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("alice");
            when(userReadService.findUserByUsername("alice")).thenReturn(Optional.of(user));
            when(redisRepository.findByUserPhone("9876543210")).thenReturn(List.of(older, newer));

            Optional<CallNotificationResponse> result = service.getRecentNotificationsFromRedis();

            assertTrue(result.isPresent());
            assertEquals("new", result.get().getCallSid());
        }
    }

    @Test
    void sendNotificationAsync_savesAndDispatches() {
        CallNotificationResponse notification = CallNotificationResponse.builder()
                .callSid("sid-1")
                .createdAt(LocalDateTime.now())
                .build();

        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        when(userReadService.findUsersByPersonPhoneNumber("1234567890"))
                .thenReturn(List.of(user));

        service.sendNotificationAsync(notification, "1234567890");

        verify(redisRepository).save(notification, "1234567890");
        verify(sseService).sendNotificationToUser(notification, "alice");
    }
}
