package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvisorAddressReadServiceImplTest {

    private static final String ADVISOR_USERNAME = "adv_user";

    @Mock
    private AdvisorRepositoryWrapper advisorRepositoryWrapper;

    @Mock
    private UserReadService userReadService;

    @InjectMocks
    private AdvisorAddressReadServiceImpl advisorAddressReadService;

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
    void getAddresses_success_delegatesToUserReadService() {
        List<AddressData> expected = List.of(new AddressData());
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(userReadService.getAddressesForUser(ADVISOR_USERNAME)).thenReturn(expected);

        List<AddressData> result = advisorAddressReadService.getAddresses(advisorIdentifier);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userReadService).getAddressesForUser(ADVISOR_USERNAME);
    }

    @Test
    void getAddress_success_returnsAddressData() {
        String addressId = "addr-1";
        AddressData expected = new AddressData();
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(userReadService.getAddressForUser(ADVISOR_USERNAME, addressId)).thenReturn(expected);

        AddressData result = advisorAddressReadService.getAddress(advisorIdentifier, addressId);

        assertNotNull(result);
        assertEquals(expected, result);
        verify(userReadService).getAddressForUser(ADVISOR_USERNAME, addressId);
    }

    @Test
    void getAddress_userServiceThrowsResponseStatusException_throwsNotFound() {
        String addressId = "addr-1";
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(userReadService.getAddressForUser(ADVISOR_USERNAME, addressId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> advisorAddressReadService.getAddress(advisorIdentifier, addressId));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
