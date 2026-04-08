package com.nivasafinance.features.master.pincode.service.impl;

import com.nivasafinance.common.base.model.MasterLanguageData;
import com.nivasafinance.features.master.location.entity.Country;
import com.nivasafinance.features.master.location.entity.District;
import com.nivasafinance.features.master.location.entity.State;
import com.nivasafinance.features.master.location.entity.Taluka;
import com.nivasafinance.features.master.location.repository.CountryRepository;
import com.nivasafinance.features.master.location.repository.DistrictRepository;
import com.nivasafinance.features.master.location.repository.StateRepository;
import com.nivasafinance.features.master.location.repository.TalukaRepository;
import com.nivasafinance.features.master.pincode.dto.PincodeResponse;
import com.nivasafinance.features.master.pincode.entity.Pincode;
import com.nivasafinance.features.master.pincode.exception.PincodeNotFoundException;
import com.nivasafinance.features.master.pincode.repository.PincodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PincodeServiceImplTest {

    @Mock
    private PincodeRepository pincodeRepository;

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private StateRepository stateRepository;

    @Mock
    private DistrictRepository districtRepository;

    @Mock
    private TalukaRepository talukaRepository;

    @Mock
    private MessageSource messageSource;

    private PincodeServiceImpl pincodeService;

    private static final String TEST_PINCODE = "560001";

    private Pincode pincode;
    private Country country;
    private State state;
    private District district;
    private Taluka taluka;

    @BeforeEach
    void setUp() {
        pincodeService = new PincodeServiceImpl(
                pincodeRepository, countryRepository, stateRepository,
                districtRepository, talukaRepository, messageSource);

        pincode = new Pincode();
        pincode.setId(1L);
        pincode.setPincode(TEST_PINCODE);
        pincode.setCountryId(1L);
        pincode.setStateId(10L);
        pincode.setDistrictId(100L);
        pincode.setTalukaId(1000L);
        pincode.setIsServicable(true);

        country = new Country();
        country.setId(1L);
        country.setName("India");
        country.setCode("IN");

        state = new State();
        state.setId(10L);
        state.setName("Karnataka");
        state.setCode("KA");

        district = new District();
        district.setId(100L);
        district.setName("Bangalore Urban");
        district.setCode("BLR");
        district.setNameValues(MasterLanguageData.builder().defaultValue("Bangalore Urban").build());

        taluka = new Taluka();
        taluka.setId(1000L);
        taluka.setName("Bangalore North");
        taluka.setCode("BN");
        taluka.setNameValues(MasterLanguageData.builder().defaultValue("Bangalore North").build());
    }

    // ── getPincodeDetails ────────────────────────────────────────────

    @Test
    void getPincodeDetails_allLocationIds_returnsFullResponse() {
        when(pincodeRepository.findAllByPincode(TEST_PINCODE)).thenReturn(List.of(pincode));
        when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
        when(stateRepository.findById(10L)).thenReturn(Optional.of(state));
        when(districtRepository.findById(100L)).thenReturn(Optional.of(district));
        when(talukaRepository.findById(1000L)).thenReturn(Optional.of(taluka));

        PincodeResponse result = pincodeService.getPincodeDetails(TEST_PINCODE);

        assertEquals(TEST_PINCODE, result.getPincode(), "Pincode should match the requested value");
        assertEquals("India", result.getCountry(), "Country name should be resolved from country entity");
        assertEquals("IN", result.getCountryCode(), "Country code should be resolved from country entity");
        assertEquals("Karnataka", result.getState(), "State name should be resolved from state entity");
        assertEquals("KA", result.getStateCode(), "State code should be resolved from state entity");
        assertTrue(result.getIsServicable(), "Serviceable flag should match the pincode entity");
    }

    @Test
    void getPincodeDetails_allLocationIds_returnsDistrictAndTalukaDetails() {
        when(pincodeRepository.findAllByPincode(TEST_PINCODE)).thenReturn(List.of(pincode));
        when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
        when(stateRepository.findById(10L)).thenReturn(Optional.of(state));
        when(districtRepository.findById(100L)).thenReturn(Optional.of(district));
        when(talukaRepository.findById(1000L)).thenReturn(Optional.of(taluka));

        PincodeResponse result = pincodeService.getPincodeDetails(TEST_PINCODE);

        assertEquals("BLR", result.getDistrictCode(), "District code should be resolved");
        assertEquals("BN", result.getTalukaCode(), "Taluka code should be resolved");
        assertEquals(100L, result.getDistrictId(), "District ID should be set from pincode entity");
        assertEquals(1000L, result.getTalukaId(), "Taluka ID should be set from pincode entity");
    }

    @Test
    void getPincodeDetails_pincodeNotFound_throwsPincodeNotFound() {
        when(pincodeRepository.findAllByPincode("999999")).thenReturn(Collections.emptyList());

        assertThrows(PincodeNotFoundException.class,
                () -> pincodeService.getPincodeDetails("999999"),
                "Non-existent pincode should throw PincodeNotFoundException");
    }

    @Test
    void getPincodeDetails_nullCountryId_countryFieldsNull() {
        pincode.setCountryId(null);
        when(pincodeRepository.findAllByPincode(TEST_PINCODE)).thenReturn(List.of(pincode));
        when(stateRepository.findById(10L)).thenReturn(Optional.of(state));
        when(districtRepository.findById(100L)).thenReturn(Optional.of(district));
        when(talukaRepository.findById(1000L)).thenReturn(Optional.of(taluka));

        PincodeResponse result = pincodeService.getPincodeDetails(TEST_PINCODE);

        assertNull(result.getCountry(), "Country name should be null when countryId is null");
        assertNull(result.getCountryCode(), "Country code should be null when countryId is null");
        assertNull(result.getCountryId(), "Country ID should be null in response");
        verify(countryRepository, never()).findById(anyLong());
    }

    @Test
    void getPincodeDetails_nullStateId_stateFieldsNull() {
        pincode.setStateId(null);
        when(pincodeRepository.findAllByPincode(TEST_PINCODE)).thenReturn(List.of(pincode));
        when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
        when(districtRepository.findById(100L)).thenReturn(Optional.of(district));
        when(talukaRepository.findById(1000L)).thenReturn(Optional.of(taluka));

        PincodeResponse result = pincodeService.getPincodeDetails(TEST_PINCODE);

        assertNull(result.getState(), "State name should be null when stateId is null");
        assertNull(result.getStateCode(), "State code should be null when stateId is null");
        verify(stateRepository, never()).findById(anyLong());
    }

    @Test
    void getPincodeDetails_nullDistrictId_districtFieldsNull() {
        pincode.setDistrictId(null);
        when(pincodeRepository.findAllByPincode(TEST_PINCODE)).thenReturn(List.of(pincode));
        when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
        when(stateRepository.findById(10L)).thenReturn(Optional.of(state));
        when(talukaRepository.findById(1000L)).thenReturn(Optional.of(taluka));

        PincodeResponse result = pincodeService.getPincodeDetails(TEST_PINCODE);

        assertNull(result.getDistrict(), "District name should be null when districtId is null");
        assertNull(result.getDistrictCode(), "District code should be null when districtId is null");
        verify(districtRepository, never()).findById(anyLong());
    }

    @Test
    void getPincodeDetails_nullTalukaId_talukaFieldsNull() {
        pincode.setTalukaId(null);
        when(pincodeRepository.findAllByPincode(TEST_PINCODE)).thenReturn(List.of(pincode));
        when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
        when(stateRepository.findById(10L)).thenReturn(Optional.of(state));
        when(districtRepository.findById(100L)).thenReturn(Optional.of(district));

        PincodeResponse result = pincodeService.getPincodeDetails(TEST_PINCODE);

        assertNull(result.getTaluka(), "Taluka name should be null when talukaId is null");
        assertNull(result.getTalukaCode(), "Taluka code should be null when talukaId is null");
        verify(talukaRepository, never()).findById(anyLong());
    }

    @Test
    void getPincodeDetails_allLocationIdsNull_allLocationFieldsNull() {
        pincode.setCountryId(null);
        pincode.setStateId(null);
        pincode.setDistrictId(null);
        pincode.setTalukaId(null);
        when(pincodeRepository.findAllByPincode(TEST_PINCODE)).thenReturn(List.of(pincode));

        PincodeResponse result = pincodeService.getPincodeDetails(TEST_PINCODE);

        assertNull(result.getCountry(), "Country should be null when all location IDs are null");
        assertNull(result.getState(), "State should be null when all location IDs are null");
        assertNull(result.getDistrict(), "District should be null when all location IDs are null");
        assertNull(result.getTaluka(), "Taluka should be null when all location IDs are null");
        assertEquals(TEST_PINCODE, result.getPincode(), "Pincode value should still be present");
    }

    @Test
    void getPincodeDetails_countryIdPresentButNotFound_countryFieldsNull() {
        when(pincodeRepository.findAllByPincode(TEST_PINCODE)).thenReturn(List.of(pincode));
        when(countryRepository.findById(1L)).thenReturn(Optional.empty());
        when(stateRepository.findById(10L)).thenReturn(Optional.of(state));
        when(districtRepository.findById(100L)).thenReturn(Optional.of(district));
        when(talukaRepository.findById(1000L)).thenReturn(Optional.of(taluka));

        PincodeResponse result = pincodeService.getPincodeDetails(TEST_PINCODE);

        assertNull(result.getCountry(),
                "Country name should be null when country entity is not found in DB");
        assertNull(result.getCountryCode(),
                "Country code should be null when country entity is not found in DB");
    }

    @Test
    void getPincodeDetails_stateIdPresentButNotFound_stateFieldsNull() {
        when(pincodeRepository.findAllByPincode(TEST_PINCODE)).thenReturn(List.of(pincode));
        when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
        when(stateRepository.findById(10L)).thenReturn(Optional.empty());
        when(districtRepository.findById(100L)).thenReturn(Optional.of(district));
        when(talukaRepository.findById(1000L)).thenReturn(Optional.of(taluka));

        PincodeResponse result = pincodeService.getPincodeDetails(TEST_PINCODE);

        assertNull(result.getState(),
                "State name should be null when state entity is not found in DB");
    }

    @Test
    void getPincodeDetails_districtIdPresentButNotFound_districtFieldsNull() {
        when(pincodeRepository.findAllByPincode(TEST_PINCODE)).thenReturn(List.of(pincode));
        when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
        when(stateRepository.findById(10L)).thenReturn(Optional.of(state));
        when(districtRepository.findById(100L)).thenReturn(Optional.empty());
        when(talukaRepository.findById(1000L)).thenReturn(Optional.of(taluka));

        PincodeResponse result = pincodeService.getPincodeDetails(TEST_PINCODE);

        assertNull(result.getDistrict(),
                "District name should be null when district entity is not found in DB");
    }

    @Test
    void getPincodeDetails_talukaIdPresentButNotFound_talukaFieldsNull() {
        when(pincodeRepository.findAllByPincode(TEST_PINCODE)).thenReturn(List.of(pincode));
        when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
        when(stateRepository.findById(10L)).thenReturn(Optional.of(state));
        when(districtRepository.findById(100L)).thenReturn(Optional.of(district));
        when(talukaRepository.findById(1000L)).thenReturn(Optional.empty());

        PincodeResponse result = pincodeService.getPincodeDetails(TEST_PINCODE);

        assertNull(result.getTaluka(),
                "Taluka name should be null when taluka entity is not found in DB");
    }

    @Test
    void getPincodeDetails_multiplePincodeRecords_usesFirstRecord() {
        Pincode secondPincode = new Pincode();
        secondPincode.setId(2L);
        secondPincode.setPincode(TEST_PINCODE);
        secondPincode.setCountryId(null);
        secondPincode.setStateId(null);
        secondPincode.setDistrictId(null);
        secondPincode.setTalukaId(null);
        secondPincode.setIsServicable(false);

        when(pincodeRepository.findAllByPincode(TEST_PINCODE)).thenReturn(List.of(pincode, secondPincode));
        when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
        when(stateRepository.findById(10L)).thenReturn(Optional.of(state));
        when(districtRepository.findById(100L)).thenReturn(Optional.of(district));
        when(talukaRepository.findById(1000L)).thenReturn(Optional.of(taluka));

        PincodeResponse result = pincodeService.getPincodeDetails(TEST_PINCODE);

        assertTrue(result.getIsServicable(),
                "Should use first pincode record's serviceable flag when multiple records exist");
        assertEquals(1L, result.getCountryId(),
                "Should use first pincode record's country ID when multiple records exist");
    }
}
