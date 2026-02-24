package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.AddressRequest;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.repository.PersonRepositoryWrapper;
import com.nivasafinance.features.person.service.PersonWriteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvisorAddressWriteServiceImplTest {

    @Mock
    private AdvisorRepositoryWrapper advisorRepositoryWrapper;

    @Mock
    private PersonWriteService personWriteService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private PersonRepositoryWrapper personRepositoryWrapper;

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
        advisor.setPersonId(2L);
    }

    @Test
    void addAddress_success_returnsAddressId() {
        AddressRequest request = new AddressRequest();
        String addressId = "addr-123";
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(personWriteService.addAddress(2L, request)).thenReturn(addressId);
        when(personRepositoryWrapper.findByIdWithException(2L)).thenReturn(new Person());

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");

            String result = advisorAddressWriteService.addAddress(advisorIdentifier, request);

            assertEquals(addressId, result);
            verify(personWriteService).addAddress(2L, request);
            verify(applicationEventPublisher).publishEvent(any(Object.class));
        }
    }

    @Test
    void updateAddress_success_returnsAddressData() {
        String addressId = "addr-1";
        AddressRequest request = new AddressRequest();
        AddressData expected = new AddressData();
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(personWriteService.updateAddress(2L, addressId, request)).thenReturn(expected);
        when(personRepositoryWrapper.findByIdWithException(2L)).thenReturn(new Person());

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");

            AddressData result = advisorAddressWriteService.updateAddress(advisorIdentifier, addressId, request);

            assertNotNull(result);
            assertEquals(expected, result);
            verify(personWriteService).updateAddress(2L, addressId, request);
            verify(applicationEventPublisher).publishEvent(any(Object.class));
        }
    }
}
