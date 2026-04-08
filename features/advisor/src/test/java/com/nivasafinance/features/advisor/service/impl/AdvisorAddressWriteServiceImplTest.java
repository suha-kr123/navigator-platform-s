package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.AddressRequest;
import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import com.nivasafinance.features.usermanagement.service.UserWriteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvisorAddressWriteServiceImplTest {

    private static final String ADVISOR_USERNAME = "adv_user";

    @Mock
    private AdvisorRepositoryWrapper advisorRepositoryWrapper;

    @Mock
    private UserWriteService userWriteService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private UserReadService userReadService;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private AdvisorAddressWriteServiceImpl advisorAddressWriteService;

    private UUID advisorIdentifier;
    private Advisor advisor;

    @BeforeEach
    void setUp() {
        advisorIdentifier = UUID.randomUUID();
        advisor = new Advisor();
        advisor.setId(1L);
        advisor.setIdentifier(advisorIdentifier);
        advisor.setUsername(ADVISOR_USERNAME);
    }

    @Test
    void addAddress_success_returnsAddressId() {
        AddressRequest request = new AddressRequest();
        String addressId = "addr-123";
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(userWriteService.addAddressForUser(ADVISOR_USERNAME, request)).thenReturn(addressId);
        when(userReadService.getPersonForUser(ADVISOR_USERNAME)).thenReturn(
                PersonResponse.builder().mobileNumbers(List.of(new MobileNumberDetails("9999999999", true, false))).build());

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");

            String result = advisorAddressWriteService.addAddress(advisorIdentifier, request);

            assertEquals(addressId, result);
            verify(userWriteService).addAddressForUser(ADVISOR_USERNAME, request);
            verify(applicationEventPublisher).publishEvent(any(Object.class));
        }
    }

    @Test
    void updateAddress_success_returnsAddressData() {
        String addressId = "addr-1";
        AddressRequest request = new AddressRequest();
        request.setAddress("Line 1");
        request.setPincode(new AddressRequest.PincodeRequest("560001", null, null));
        AddressData expected = new AddressData();
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(userWriteService.updateAddressForUser(ADVISOR_USERNAME, addressId, request)).thenReturn(expected);
        when(userReadService.getPersonForUser(ADVISOR_USERNAME)).thenReturn(
                PersonResponse.builder().mobileNumbers(List.of(new MobileNumberDetails("9999999999", true, false))).build());

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");

            AddressData result = advisorAddressWriteService.updateAddress(advisorIdentifier, addressId, request);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(userWriteService).updateAddressForUser(ADVISOR_USERNAME, addressId, request);
            verify(applicationEventPublisher).publishEvent(any(Object.class));
        }
    }

    @Test
    void updateAddress_blankAddress_throwsBeforeUserWrite() {
        AddressRequest request = new AddressRequest();
        request.setAddress("  ");
        request.setPincode(new AddressRequest.PincodeRequest("560001", null, null));
        when(messageSource.getMessage(any(), any(), any())).thenReturn("validation error");

        assertThrows(BadRequestException.class,
                () -> advisorAddressWriteService.updateAddress(advisorIdentifier, "addr-1", request));

        verify(userWriteService, never()).updateAddressForUser(any(), any(), any());
    }

    @Test
    void addAddress_personWithoutPrimaryMobile_stillPublishesEvent() {
        AddressRequest request = new AddressRequest();
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(userWriteService.addAddressForUser(ADVISOR_USERNAME, request)).thenReturn("new-id");
        when(userReadService.getPersonForUser(ADVISOR_USERNAME)).thenReturn(
                PersonResponse.builder()
                        .mobileNumbers(List.of(new MobileNumberDetails("8888888888", false, false)))
                        .build());

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");

            advisorAddressWriteService.addAddress(advisorIdentifier, request);

            verify(applicationEventPublisher).publishEvent(any(Object.class));
        }
    }

    @Test
    void addAddress_personWithNullMobileNumbers_stillPublishesEvent() {
        AddressRequest request = new AddressRequest();
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(userWriteService.addAddressForUser(ADVISOR_USERNAME, request)).thenReturn("new-id");
        when(userReadService.getPersonForUser(ADVISOR_USERNAME)).thenReturn(
                PersonResponse.builder().mobileNumbers(null).build());

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");

            advisorAddressWriteService.addAddress(advisorIdentifier, request);

            verify(applicationEventPublisher).publishEvent(any(Object.class));
        }
    }
}
