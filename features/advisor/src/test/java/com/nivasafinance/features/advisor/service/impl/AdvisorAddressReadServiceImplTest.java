package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.person.service.PersonReadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvisorAddressReadServiceImplTest {

    @Mock
    private AdvisorRepositoryWrapper advisorRepositoryWrapper;

    @Mock
    private PersonReadService personReadService;

    @InjectMocks
    private AdvisorAddressReadServiceImpl advisorAddressReadService;

    private static final Long TEST_PERSON_ID = 2L;

    private UUID advisorIdentifier;
    private Advisor advisor;

    @BeforeEach
    void setUp() {
        advisorIdentifier = UUID.randomUUID();
        advisor = new Advisor();
        advisor.setId(1L);
        advisor.setIdentifier(advisorIdentifier);
        advisor.setPersonId(TEST_PERSON_ID);
    }

    @Test
    void getAddresses_success_delegatesToPersonReadService() {
        List<AddressData> expected = List.of(new AddressData());
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(personReadService.getAddresses(TEST_PERSON_ID)).thenReturn(expected);

        List<AddressData> result = advisorAddressReadService.getAddresses(advisorIdentifier);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(personReadService).getAddresses(TEST_PERSON_ID);
    }

    @Test
    void getAddress_success_returnsAddressData() {
        String addressId = "addr-1";
        AddressData expected = new AddressData();
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(personReadService.getAddress(TEST_PERSON_ID, addressId)).thenReturn(expected);

        AddressData result = advisorAddressReadService.getAddress(advisorIdentifier, addressId);

        assertNotNull(result);
        assertEquals(expected, result);
        verify(personReadService).getAddress(TEST_PERSON_ID, addressId);
    }

    @Test
    void getAddress_personThrowsResponseStatusException_throwsNotFound() {
        String addressId = "addr-1";
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(personReadService.getAddress(TEST_PERSON_ID, addressId)).thenThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Not found"));

        assertThrows(ResponseStatusException.class,
                () -> advisorAddressReadService.getAddress(advisorIdentifier, addressId));
    }
}
