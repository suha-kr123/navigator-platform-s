package com.nivasafinance.features.address.service.impl;

import com.nivasafinance.common.base.model.MasterLanguageData;
import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.AddressRequest;
import com.nivasafinance.common.enums.AddressType;
import com.nivasafinance.features.master.location.dto.LocationDisplayNames;
import com.nivasafinance.features.master.location.entity.Country;
import com.nivasafinance.features.master.location.entity.District;
import com.nivasafinance.features.master.location.entity.State;
import com.nivasafinance.features.master.location.entity.Taluka;
import com.nivasafinance.features.master.location.entity.Village;
import com.nivasafinance.features.master.location.repository.CountryRepository;
import com.nivasafinance.features.master.location.repository.DistrictRepository;
import com.nivasafinance.features.master.location.repository.LocationRepository;
import com.nivasafinance.features.master.location.repository.StateRepository;
import com.nivasafinance.features.master.location.repository.TalukaRepository;
import com.nivasafinance.features.master.location.repository.VillageRepository;
import com.nivasafinance.features.master.pincode.dto.PincodeResponse;
import com.nivasafinance.features.master.pincode.service.PincodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressDataServiceImplTest {

    @Mock
    private PincodeService pincodeService;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private StateRepository stateRepository;

    @Mock
    private DistrictRepository districtRepository;

    @Mock
    private TalukaRepository talukaRepository;

    @Mock
    private VillageRepository villageRepository;

    @InjectMocks
    private AddressDataServiceImpl addressDataService;

    private PincodeResponse pincodeResponse;

    @BeforeEach
    void setUp() {
        pincodeResponse = PincodeResponse.builder()
                .pincode("560001")
                .district("Bangalore Urban")
                .state("Karnataka")
                .country("India")
                .taluka("Bangalore South")
                .districtCode("DIST01")
                .stateCode("KA")
                .countryCode("IN")
                .talukaCode("TAL01")
                .districtId(10L)
                .stateId(5L)
                .countryId(1L)
                .talukaId(20L)
                .isServicable(true)
                .build();
    }

    // ==================== createAddressData — Pincode Path (Priority 1) ====================

    @Test
    void createAddressData_withPincode_populatesFromPincodeResponse() {
        AddressRequest request = new AddressRequest();
        request.setAddress("123 MG Road");
        request.setPincode(new AddressRequest.PincodeRequest("560001", null, null));

        when(pincodeService.getPincodeDetails("560001")).thenReturn(pincodeResponse);

        AddressData result = addressDataService.createAddressData(request);

        assertNotNull(result);
        assertEquals("123 MG Road", result.getAddress());
        assertEquals("560001", result.getPincode());
        assertEquals("Bangalore Urban", result.getDistrict());
        assertEquals("Karnataka", result.getState());
        assertEquals("India", result.getCountry());
        assertEquals("Bangalore South", result.getTaluka());
        assertEquals("DIST01", result.getDistrictCode());
        assertEquals("KA", result.getStateCode());
        assertEquals("IN", result.getCountryCode());
        assertEquals("TAL01", result.getTalukaCode());
        assertEquals(10L, result.getDistrictId());
        assertEquals(5L, result.getStateId());
        assertEquals(1L, result.getCountryId());
        assertEquals(20L, result.getTalukaId());
        assertTrue(result.getIsServiceable());
        verify(pincodeService).getPincodeDetails("560001");
    }

    @Test
    void createAddressData_withPincodeAndVillageCode_looksUpVillage() {
        AddressRequest request = new AddressRequest();
        request.setAddress("Village Road");
        request.setPincode(new AddressRequest.PincodeRequest("560001", "VIL01", null));

        Village village = new Village();
        village.setId(100L);
        village.setCode("VIL01");
        village.setNameValues(new MasterLanguageData("Test Village", null));

        when(pincodeService.getPincodeDetails("560001")).thenReturn(pincodeResponse);
        when(villageRepository.findByCodeAndTalukaIdAndIsActiveTrue("VIL01", 20L))
                .thenReturn(Optional.of(village));

        AddressData result = addressDataService.createAddressData(request);

        assertEquals("VIL01", result.getVillageCode());
        assertEquals(100L, result.getVillageId());
        assertNotNull(result.getVillageName());
        verify(villageRepository).findByCodeAndTalukaIdAndIsActiveTrue("VIL01", 20L);
    }

    @Test
    void createAddressData_withPincodeAndVillageName_setsFreeTextVillageName() {
        AddressRequest request = new AddressRequest();
        request.setAddress("Village Road");
        request.setPincode(new AddressRequest.PincodeRequest("560001", null, "My Village"));

        when(pincodeService.getPincodeDetails("560001")).thenReturn(pincodeResponse);

        AddressData result = addressDataService.createAddressData(request);

        assertEquals("My Village", result.getVillageName());
        verify(villageRepository, never()).findByCodeAndTalukaIdAndIsActiveTrue(any(), any());
    }

    @Test
    void createAddressData_withPincodeAndVillageCodeButNullTalukaId_fallsToVillageName() {
        pincodeResponse.setTalukaId(null);
        AddressRequest request = new AddressRequest();
        request.setPincode(new AddressRequest.PincodeRequest("560001", "VIL01", "Fallback Village"));

        when(pincodeService.getPincodeDetails("560001")).thenReturn(pincodeResponse);

        AddressData result = addressDataService.createAddressData(request);

        assertEquals("Fallback Village", result.getVillageName());
        verify(villageRepository, never()).findByCodeAndTalukaIdAndIsActiveTrue(any(), any());
    }

    @Test
    void createAddressData_withPincodeAndVillageNotFound_villagFieldsRemainNull() {
        AddressRequest request = new AddressRequest();
        request.setPincode(new AddressRequest.PincodeRequest("560001", "UNKNOWN", null));

        when(pincodeService.getPincodeDetails("560001")).thenReturn(pincodeResponse);
        when(villageRepository.findByCodeAndTalukaIdAndIsActiveTrue("UNKNOWN", 20L))
                .thenReturn(Optional.empty());

        AddressData result = addressDataService.createAddressData(request);

        assertNull(result.getVillageCode());
        assertNull(result.getVillageId());
        assertNull(result.getVillageName());
    }

    @Test
    void createAddressData_pincodeLookupFails_continuesWithNullValues() {
        AddressRequest request = new AddressRequest();
        request.setAddress("123 Street");
        request.setPincode(new AddressRequest.PincodeRequest("999999", null, null));

        when(pincodeService.getPincodeDetails("999999")).thenThrow(new RuntimeException("Service unavailable"));

        AddressData result = addressDataService.createAddressData(request);

        assertNotNull(result);
        assertEquals("123 Street", result.getAddress());
        assertEquals("999999", result.getPincode());
        assertNull(result.getDistrict());
        assertNull(result.getState());
        assertNull(result.getCountry());
        assertFalse(result.getIsServiceable());
    }

    @Test
    void createAddressData_villageLookupFails_continuesGracefully() {
        AddressRequest request = new AddressRequest();
        request.setPincode(new AddressRequest.PincodeRequest("560001", "VIL01", null));

        when(pincodeService.getPincodeDetails("560001")).thenReturn(pincodeResponse);
        when(villageRepository.findByCodeAndTalukaIdAndIsActiveTrue("VIL01", 20L))
                .thenThrow(new RuntimeException("DB error"));

        AddressData result = addressDataService.createAddressData(request);

        assertNotNull(result);
        assertEquals("Bangalore Urban", result.getDistrict());
        assertNull(result.getVillageCode());
    }

    // ==================== createAddressData — Location Code Path (Priority 2) ====================

    @Test
    void createAddressData_withLocationCodes_fullHierarchyLookup() {
        AddressRequest.AddressLocationRequest location = new AddressRequest.AddressLocationRequest(
                "DIST01", "KA", "IN", "TAL01", "VIL01", null);
        AddressRequest request = new AddressRequest();
        request.setAddress("Location Street");
        request.setLocation(location);

        Country country = new Country();
        country.setId(1L);
        country.setCode("IN");
        country.setName("India");

        State state = new State();
        state.setId(5L);
        state.setCode("KA");
        state.setName("Karnataka");

        District district = new District();
        district.setId(10L);
        district.setCode("DIST01");
        district.setNameValues(new MasterLanguageData("Bangalore Urban", null));

        Taluka taluka = new Taluka();
        taluka.setId(20L);
        taluka.setCode("TAL01");
        taluka.setNameValues(new MasterLanguageData("Bangalore South", null));

        Village village = new Village();
        village.setId(100L);
        village.setCode("VIL01");
        village.setNameValues(new MasterLanguageData("Test Village", null));

        when(countryRepository.findByCodeAndIsActiveTrue("IN")).thenReturn(Optional.of(country));
        when(stateRepository.findByCodeAndCountryIdAndIsActiveTrue("KA", 1L)).thenReturn(Optional.of(state));
        when(districtRepository.findByCodeAndStateIdAndIsActiveTrue("DIST01", 5L)).thenReturn(Optional.of(district));
        when(talukaRepository.findByCodeAndDistrictIdAndIsActiveTrue("TAL01", 10L)).thenReturn(Optional.of(taluka));
        when(villageRepository.findByCodeAndTalukaIdAndIsActiveTrue("VIL01", 20L)).thenReturn(Optional.of(village));

        AddressData result = addressDataService.createAddressData(request);

        assertNotNull(result);
        assertEquals("Location Street", result.getAddress());
        assertEquals("IN", result.getCountryCode());
        assertEquals(1L, result.getCountryId());
        assertEquals("India", result.getCountry());
        assertEquals("KA", result.getStateCode());
        assertEquals(5L, result.getStateId());
        assertEquals("Karnataka", result.getState());
        assertEquals("DIST01", result.getDistrictCode());
        assertEquals(10L, result.getDistrictId());
        assertEquals("TAL01", result.getTalukaCode());
        assertEquals(20L, result.getTalukaId());
        assertEquals("VIL01", result.getVillageCode());
        assertEquals(100L, result.getVillageId());
    }

    @Test
    void createAddressData_locationCodeCountryNotFound_stopsHierarchy() {
        AddressRequest.AddressLocationRequest location = new AddressRequest.AddressLocationRequest(
                "DIST01", "KA", "UNKNOWN", "TAL01", null, null);
        AddressRequest request = new AddressRequest();
        request.setLocation(location);

        when(countryRepository.findByCodeAndIsActiveTrue("UNKNOWN")).thenReturn(Optional.empty());

        AddressData result = addressDataService.createAddressData(request);

        assertNull(result.getCountryCode());
        assertNull(result.getStateCode());
        assertNull(result.getDistrictCode());
        verify(stateRepository, never()).findByCodeAndCountryIdAndIsActiveTrue(any(), any());
        verify(districtRepository, never()).findByCodeAndStateIdAndIsActiveTrue(any(), any());
    }

    @Test
    void createAddressData_locationCodeStateNotFound_stopsAtCountry() {
        AddressRequest.AddressLocationRequest location = new AddressRequest.AddressLocationRequest(
                "DIST01", "UNKNOWN", "IN", null, null, null);
        AddressRequest request = new AddressRequest();
        request.setLocation(location);

        Country country = new Country();
        country.setId(1L);
        country.setCode("IN");
        country.setName("India");

        when(countryRepository.findByCodeAndIsActiveTrue("IN")).thenReturn(Optional.of(country));
        when(stateRepository.findByCodeAndCountryIdAndIsActiveTrue("UNKNOWN", 1L)).thenReturn(Optional.empty());

        AddressData result = addressDataService.createAddressData(request);

        assertEquals("IN", result.getCountryCode());
        assertEquals("India", result.getCountry());
        assertNull(result.getStateCode());
        assertNull(result.getDistrictCode());
        verify(districtRepository, never()).findByCodeAndStateIdAndIsActiveTrue(any(), any());
    }

    @Test
    void createAddressData_locationWithFreeTextVillageName_setsVillageName() {
        AddressRequest.AddressLocationRequest location = new AddressRequest.AddressLocationRequest(
                null, null, null, null, null, "My Free Village");
        AddressRequest request = new AddressRequest();
        request.setLocation(location);

        AddressData result = addressDataService.createAddressData(request);

        assertEquals("My Free Village", result.getVillageName());
    }

    @Test
    void createAddressData_locationCodeLookupThrowsException_continuesGracefully() {
        AddressRequest.AddressLocationRequest location = new AddressRequest.AddressLocationRequest(
                null, null, "IN", null, null, null);
        AddressRequest request = new AddressRequest();
        request.setLocation(location);

        when(countryRepository.findByCodeAndIsActiveTrue("IN")).thenThrow(new RuntimeException("DB error"));

        AddressData result = addressDataService.createAddressData(request);

        assertNotNull(result);
        assertNull(result.getCountryCode());
        assertFalse(result.getIsServiceable());
    }

    @Test
    void createAddressData_locationVillageCodeWithoutTaluka_skipsVillageLookup() {
        AddressRequest.AddressLocationRequest location = new AddressRequest.AddressLocationRequest(
                null, null, "IN", null, "VIL01", null);
        AddressRequest request = new AddressRequest();
        request.setLocation(location);

        Country country = new Country();
        country.setId(1L);
        country.setCode("IN");
        country.setName("India");
        when(countryRepository.findByCodeAndIsActiveTrue("IN")).thenReturn(Optional.of(country));

        AddressData result = addressDataService.createAddressData(request);

        assertNull(result.getVillageCode());
        verify(villageRepository, never()).findByCodeAndTalukaIdAndIsActiveTrue(any(), any());
    }

    @Test
    void createAddressData_locationStateLookupThrows_continuesWithoutState() {
        AddressRequest.AddressLocationRequest location = new AddressRequest.AddressLocationRequest(
                "DIST01", "KA", "IN", null, null, null);
        AddressRequest request = new AddressRequest();
        request.setLocation(location);

        Country country = new Country();
        country.setId(1L);
        country.setCode("IN");
        country.setName("India");

        when(countryRepository.findByCodeAndIsActiveTrue("IN")).thenReturn(Optional.of(country));
        when(stateRepository.findByCodeAndCountryIdAndIsActiveTrue("KA", 1L))
                .thenThrow(new RuntimeException("State DB error"));

        AddressData result = addressDataService.createAddressData(request);

        assertEquals("IN", result.getCountryCode());
        assertNull(result.getStateCode());
        assertNull(result.getDistrictCode());
        verify(districtRepository, never()).findByCodeAndStateIdAndIsActiveTrue(any(), any());
    }

    @Test
    void createAddressData_locationDistrictLookupThrows_continuesWithoutDistrict() {
        AddressRequest.AddressLocationRequest location = new AddressRequest.AddressLocationRequest(
                "DIST01", "KA", "IN", "TAL01", null, null);
        AddressRequest request = new AddressRequest();
        request.setLocation(location);

        Country country = new Country();
        country.setId(1L);
        country.setCode("IN");
        country.setName("India");
        State state = new State();
        state.setId(5L);
        state.setCode("KA");
        state.setName("Karnataka");

        when(countryRepository.findByCodeAndIsActiveTrue("IN")).thenReturn(Optional.of(country));
        when(stateRepository.findByCodeAndCountryIdAndIsActiveTrue("KA", 1L)).thenReturn(Optional.of(state));
        when(districtRepository.findByCodeAndStateIdAndIsActiveTrue("DIST01", 5L))
                .thenThrow(new RuntimeException("District DB error"));

        AddressData result = addressDataService.createAddressData(request);

        assertEquals("KA", result.getStateCode());
        assertNull(result.getDistrictCode());
        assertNull(result.getTalukaCode());
        verify(talukaRepository, never()).findByCodeAndDistrictIdAndIsActiveTrue(any(), any());
    }

    @Test
    void createAddressData_locationTalukaLookupThrows_continuesWithoutTaluka() {
        AddressRequest.AddressLocationRequest location = new AddressRequest.AddressLocationRequest(
                "DIST01", "KA", "IN", "TAL01", null, null);
        AddressRequest request = new AddressRequest();
        request.setLocation(location);

        Country country = new Country();
        country.setId(1L);
        country.setCode("IN");
        country.setName("India");
        State state = new State();
        state.setId(5L);
        state.setCode("KA");
        state.setName("Karnataka");
        District district = new District();
        district.setId(10L);
        district.setCode("DIST01");
        district.setNameValues(new MasterLanguageData("Bangalore Urban", null));

        when(countryRepository.findByCodeAndIsActiveTrue("IN")).thenReturn(Optional.of(country));
        when(stateRepository.findByCodeAndCountryIdAndIsActiveTrue("KA", 1L)).thenReturn(Optional.of(state));
        when(districtRepository.findByCodeAndStateIdAndIsActiveTrue("DIST01", 5L)).thenReturn(Optional.of(district));
        when(talukaRepository.findByCodeAndDistrictIdAndIsActiveTrue("TAL01", 10L))
                .thenThrow(new RuntimeException("Taluka DB error"));

        AddressData result = addressDataService.createAddressData(request);

        assertEquals("DIST01", result.getDistrictCode());
        assertNull(result.getTalukaCode());
    }

    @Test
    void createAddressData_locationVillageLookupThrows_continuesGracefully() {
        AddressRequest.AddressLocationRequest location = new AddressRequest.AddressLocationRequest(
                "DIST01", "KA", "IN", "TAL01", "VIL01", null);
        AddressRequest request = new AddressRequest();
        request.setLocation(location);

        Country country = new Country();
        country.setId(1L);
        country.setCode("IN");
        country.setName("India");
        State state = new State();
        state.setId(5L);
        state.setCode("KA");
        state.setName("Karnataka");
        District district = new District();
        district.setId(10L);
        district.setCode("DIST01");
        district.setNameValues(new MasterLanguageData("Bangalore Urban", null));
        Taluka taluka = new Taluka();
        taluka.setId(20L);
        taluka.setCode("TAL01");
        taluka.setNameValues(new MasterLanguageData("Bangalore South", null));

        when(countryRepository.findByCodeAndIsActiveTrue("IN")).thenReturn(Optional.of(country));
        when(stateRepository.findByCodeAndCountryIdAndIsActiveTrue("KA", 1L)).thenReturn(Optional.of(state));
        when(districtRepository.findByCodeAndStateIdAndIsActiveTrue("DIST01", 5L)).thenReturn(Optional.of(district));
        when(talukaRepository.findByCodeAndDistrictIdAndIsActiveTrue("TAL01", 10L)).thenReturn(Optional.of(taluka));
        when(villageRepository.findByCodeAndTalukaIdAndIsActiveTrue("VIL01", 20L))
                .thenThrow(new RuntimeException("Village DB error"));

        AddressData result = addressDataService.createAddressData(request);

        assertEquals("TAL01", result.getTalukaCode());
        assertNull(result.getVillageCode());
    }

    @Test
    void createAddressData_locationDistrictNotFound_stopsAtState() {
        AddressRequest.AddressLocationRequest location = new AddressRequest.AddressLocationRequest(
                "UNKNOWN", "KA", "IN", "TAL01", null, null);
        AddressRequest request = new AddressRequest();
        request.setLocation(location);

        Country country = new Country();
        country.setId(1L);
        country.setCode("IN");
        country.setName("India");
        State state = new State();
        state.setId(5L);
        state.setCode("KA");
        state.setName("Karnataka");

        when(countryRepository.findByCodeAndIsActiveTrue("IN")).thenReturn(Optional.of(country));
        when(stateRepository.findByCodeAndCountryIdAndIsActiveTrue("KA", 1L)).thenReturn(Optional.of(state));
        when(districtRepository.findByCodeAndStateIdAndIsActiveTrue("UNKNOWN", 5L)).thenReturn(Optional.empty());

        AddressData result = addressDataService.createAddressData(request);

        assertEquals("KA", result.getStateCode());
        assertNull(result.getDistrictCode());
        assertNull(result.getTalukaCode());
        verify(talukaRepository, never()).findByCodeAndDistrictIdAndIsActiveTrue(any(), any());
    }

    @Test
    void createAddressData_locationTalukaNotFound_stopsAtDistrict() {
        AddressRequest.AddressLocationRequest location = new AddressRequest.AddressLocationRequest(
                "DIST01", "KA", "IN", "UNKNOWN", "VIL01", null);
        AddressRequest request = new AddressRequest();
        request.setLocation(location);

        Country country = new Country();
        country.setId(1L);
        country.setCode("IN");
        country.setName("India");
        State state = new State();
        state.setId(5L);
        state.setCode("KA");
        state.setName("Karnataka");
        District district = new District();
        district.setId(10L);
        district.setCode("DIST01");
        district.setNameValues(new MasterLanguageData("Bangalore Urban", null));

        when(countryRepository.findByCodeAndIsActiveTrue("IN")).thenReturn(Optional.of(country));
        when(stateRepository.findByCodeAndCountryIdAndIsActiveTrue("KA", 1L)).thenReturn(Optional.of(state));
        when(districtRepository.findByCodeAndStateIdAndIsActiveTrue("DIST01", 5L)).thenReturn(Optional.of(district));
        when(talukaRepository.findByCodeAndDistrictIdAndIsActiveTrue("UNKNOWN", 10L)).thenReturn(Optional.empty());

        AddressData result = addressDataService.createAddressData(request);

        assertEquals("DIST01", result.getDistrictCode());
        assertNull(result.getTalukaCode());
        assertNull(result.getVillageCode());
        verify(villageRepository, never()).findByCodeAndTalukaIdAndIsActiveTrue(any(), any());
    }

    @Test
    void createAddressData_locationVillageNotFound_villageFieldsRemainNull() {
        AddressRequest.AddressLocationRequest location = new AddressRequest.AddressLocationRequest(
                "DIST01", "KA", "IN", "TAL01", "UNKNOWN", null);
        AddressRequest request = new AddressRequest();
        request.setLocation(location);

        Country country = new Country();
        country.setId(1L);
        country.setCode("IN");
        country.setName("India");
        State state = new State();
        state.setId(5L);
        state.setCode("KA");
        state.setName("Karnataka");
        District district = new District();
        district.setId(10L);
        district.setCode("DIST01");
        district.setNameValues(new MasterLanguageData("Bangalore Urban", null));
        Taluka taluka = new Taluka();
        taluka.setId(20L);
        taluka.setCode("TAL01");
        taluka.setNameValues(new MasterLanguageData("Bangalore South", null));

        when(countryRepository.findByCodeAndIsActiveTrue("IN")).thenReturn(Optional.of(country));
        when(stateRepository.findByCodeAndCountryIdAndIsActiveTrue("KA", 1L)).thenReturn(Optional.of(state));
        when(districtRepository.findByCodeAndStateIdAndIsActiveTrue("DIST01", 5L)).thenReturn(Optional.of(district));
        when(talukaRepository.findByCodeAndDistrictIdAndIsActiveTrue("TAL01", 10L)).thenReturn(Optional.of(taluka));
        when(villageRepository.findByCodeAndTalukaIdAndIsActiveTrue("UNKNOWN", 20L)).thenReturn(Optional.empty());

        AddressData result = addressDataService.createAddressData(request);

        assertEquals("TAL01", result.getTalukaCode());
        assertNull(result.getVillageCode());
        assertNull(result.getVillageId());
    }

    @Test
    void createAddressData_locationNullStateCode_skipsStateLookup() {
        AddressRequest.AddressLocationRequest location = new AddressRequest.AddressLocationRequest(
                null, null, "IN", null, null, null);
        AddressRequest request = new AddressRequest();
        request.setLocation(location);

        Country country = new Country();
        country.setId(1L);
        country.setCode("IN");
        country.setName("India");

        when(countryRepository.findByCodeAndIsActiveTrue("IN")).thenReturn(Optional.of(country));

        AddressData result = addressDataService.createAddressData(request);

        assertEquals("India", result.getCountry());
        verify(stateRepository, never()).findByCodeAndCountryIdAndIsActiveTrue(any(), any());
    }

    @Test
    void createAddressData_locationBlankCountryCode_skipsAllLookups() {
        AddressRequest.AddressLocationRequest location = new AddressRequest.AddressLocationRequest(
                null, "KA", "  ", null, null, null);
        AddressRequest request = new AddressRequest();
        request.setLocation(location);

        AddressData result = addressDataService.createAddressData(request);

        assertNull(result.getCountryCode());
        assertNull(result.getStateCode());
        verifyNoInteractions(countryRepository, stateRepository);
    }

    @Test
    void createAddressData_pincodeWithNullInnerPincode_fallsToLocationPath() {
        AddressRequest request = new AddressRequest();
        request.setPincode(new AddressRequest.PincodeRequest(null, null, null));
        request.setLocation(new AddressRequest.AddressLocationRequest(
                null, null, "IN", null, null, null));

        Country country = new Country();
        country.setId(1L);
        country.setCode("IN");
        country.setName("India");
        when(countryRepository.findByCodeAndIsActiveTrue("IN")).thenReturn(Optional.of(country));

        AddressData result = addressDataService.createAddressData(request);

        assertEquals("India", result.getCountry());
        verifyNoInteractions(pincodeService);
    }

    // ==================== enrichAddressWithDisplayNames — hasAnyLocationCode branches ====================

    @Test
    void enrichAddressWithDisplayNames_onlyStateCode_triggersLookup() {
        AddressData input = AddressData.builder()
                .stateCode("KA")
                .build();

        when(locationRepository.findDisplayNamesByCodes(null, "KA", null, null, null, null, null))
                .thenReturn(Optional.empty());

        addressDataService.enrichAddressWithDisplayNames(input);

        verify(locationRepository).findDisplayNamesByCodes(null, "KA", null, null, null, null, null);
    }

    @Test
    void enrichAddressWithDisplayNames_onlyDistrictCode_triggersLookup() {
        AddressData input = AddressData.builder()
                .districtCode("DIST01")
                .build();

        when(locationRepository.findDisplayNamesByCodes(null, null, null, "DIST01", null, null, null))
                .thenReturn(Optional.empty());

        addressDataService.enrichAddressWithDisplayNames(input);

        verify(locationRepository).findDisplayNamesByCodes(null, null, null, "DIST01", null, null, null);
    }

    @Test
    void enrichAddressWithDisplayNames_onlyTalukaCode_triggersLookup() {
        AddressData input = AddressData.builder()
                .talukaCode("TAL01")
                .build();

        when(locationRepository.findDisplayNamesByCodes(null, null, null, null, "TAL01", null, null))
                .thenReturn(Optional.empty());

        addressDataService.enrichAddressWithDisplayNames(input);

        verify(locationRepository).findDisplayNamesByCodes(null, null, null, null, "TAL01", null, null);
    }

    @Test
    void enrichAddressWithDisplayNames_onlyVillageCode_triggersLookup() {
        AddressData input = AddressData.builder()
                .villageCode("VIL01")
                .build();

        when(locationRepository.findDisplayNamesByCodes(null, null, null, null, null, "VIL01", null))
                .thenReturn(Optional.empty());

        addressDataService.enrichAddressWithDisplayNames(input);

        verify(locationRepository).findDisplayNamesByCodes(null, null, null, null, null, "VIL01", null);
    }

    @Test
    void enrichAddressWithDisplayNames_onlyRegionCode_triggersLookup() {
        AddressData input = AddressData.builder()
                .regionCode("REG01")
                .build();

        when(locationRepository.findDisplayNamesByCodes(null, null, "REG01", null, null, null, null))
                .thenReturn(Optional.empty());

        addressDataService.enrichAddressWithDisplayNames(input);

        verify(locationRepository).findDisplayNamesByCodes(null, null, "REG01", null, null, null, null);
    }

    @Test
    void enrichAddressWithDisplayNames_onlyOperatingAreaCode_triggersLookup() {
        AddressData input = AddressData.builder()
                .operatingAreaCode("OA01")
                .build();

        when(locationRepository.findDisplayNamesByCodes(null, null, null, null, null, null, "OA01"))
                .thenReturn(Optional.empty());

        addressDataService.enrichAddressWithDisplayNames(input);

        verify(locationRepository).findDisplayNamesByCodes(null, null, null, null, null, null, "OA01");
    }

    @Test
    void enrichAddressWithDisplayNames_withRegionAndOperatingArea_enrichesNames() {
        AddressData input = AddressData.builder()
                .regionCode("REG01")
                .operatingAreaCode("OA01")
                .build();

        LocationDisplayNames displayNames = LocationDisplayNames.builder()
                .regionValue(new MasterLanguageData("North Karnataka", null))
                .operatingAreaValue(new MasterLanguageData("North Zone", null))
                .build();

        when(locationRepository.findDisplayNamesByCodes(null, null, "REG01", null, null, null, "OA01"))
                .thenReturn(Optional.of(displayNames));

        AddressData result = addressDataService.enrichAddressWithDisplayNames(input);

        assertEquals("North Karnataka", result.getRegion(), "Region name should be resolved from regionValue");
        assertEquals("North Zone", result.getOperatingAreaName(), "Operating area name should be resolved from operatingAreaValue");
    }

    @Test
    void enrichAddressWithDisplayNames_nullOperatingAreaValue_doesNotOverwrite() {
        AddressData input = AddressData.builder()
                .regionCode("REG01")
                .operatingAreaCode("OA01")
                .operatingAreaName("Existing Zone")
                .build();

        LocationDisplayNames displayNames = LocationDisplayNames.builder()
                .regionValue(new MasterLanguageData("North Karnataka", null))
                .operatingAreaValue(null)
                .build();

        when(locationRepository.findDisplayNamesByCodes(null, null, "REG01", null, null, null, "OA01"))
                .thenReturn(Optional.of(displayNames));

        AddressData result = addressDataService.enrichAddressWithDisplayNames(input);

        assertEquals("Existing Zone", result.getOperatingAreaName(), "Operating area name should not be overwritten when value is null");
    }

    @Test
    void enrichAddressWithDisplayNames_blankCodes_skipsLookup() {
        AddressData input = AddressData.builder()
                .countryCode("")
                .stateCode("  ")
                .districtCode("")
                .talukaCode("  ")
                .villageCode("")
                .build();

        addressDataService.enrichAddressWithDisplayNames(input);

        verifyNoInteractions(locationRepository);
    }

    // ==================== createAddressData — No Pincode / No Location ====================

    @Test
    void createAddressData_noPincodeNoLocation_returnsBasicDefaults() {
        AddressRequest request = new AddressRequest();
        request.setAddress("Simple Address");

        AddressData result = addressDataService.createAddressData(request);

        assertNotNull(result);
        assertEquals("Simple Address", result.getAddress());
        assertNull(result.getPincode());
        assertNull(result.getCountry());
        assertNull(result.getState());
        assertNull(result.getDistrict());
        assertNull(result.getTaluka());
        assertFalse(result.getIsServiceable());
        verifyNoInteractions(pincodeService, countryRepository, stateRepository,
                districtRepository, talukaRepository, villageRepository);
    }

    @Test
    void createAddressData_emptyPincode_fallsToLocationPath() {
        AddressRequest.AddressLocationRequest location = new AddressRequest.AddressLocationRequest(
                null, null, "IN", null, null, null);
        AddressRequest request = new AddressRequest();
        request.setPincode(new AddressRequest.PincodeRequest("", null, null));
        request.setLocation(location);

        Country country = new Country();
        country.setId(1L);
        country.setCode("IN");
        country.setName("India");
        when(countryRepository.findByCodeAndIsActiveTrue("IN")).thenReturn(Optional.of(country));

        AddressData result = addressDataService.createAddressData(request);

        assertEquals("India", result.getCountry());
        verifyNoInteractions(pincodeService);
    }

    @Test
    void createAddressData_blankPincode_fallsToLocationPath() {
        AddressRequest request = new AddressRequest();
        request.setPincode(new AddressRequest.PincodeRequest("   ", null, null));
        request.setLocation(new AddressRequest.AddressLocationRequest(
                null, null, "IN", null, null, null));

        Country country = new Country();
        country.setId(1L);
        country.setCode("IN");
        country.setName("India");
        when(countryRepository.findByCodeAndIsActiveTrue("IN")).thenReturn(Optional.of(country));

        AddressData result = addressDataService.createAddressData(request);

        assertEquals("India", result.getCountry());
        verifyNoInteractions(pincodeService);
    }

    @Test
    void createAddressData_nullPincodeObject_fallsToLocationPath() {
        AddressRequest request = new AddressRequest();
        request.setPincode(null);
        request.setLocation(new AddressRequest.AddressLocationRequest(
                null, null, "IN", null, null, null));

        Country country = new Country();
        country.setId(1L);
        country.setCode("IN");
        country.setName("India");
        when(countryRepository.findByCodeAndIsActiveTrue("IN")).thenReturn(Optional.of(country));

        AddressData result = addressDataService.createAddressData(request);

        assertEquals("India", result.getCountry());
        verifyNoInteractions(pincodeService);
    }

    @Test
    void createAddressData_pincodeTakesPriorityOverLocation() {
        AddressRequest request = new AddressRequest();
        request.setPincode(new AddressRequest.PincodeRequest("560001", null, null));
        request.setLocation(new AddressRequest.AddressLocationRequest(
                null, null, "IN", null, null, null));

        when(pincodeService.getPincodeDetails("560001")).thenReturn(pincodeResponse);

        AddressData result = addressDataService.createAddressData(request);

        assertEquals("India", result.getCountry());
        assertEquals("Karnataka", result.getState());
        verify(pincodeService).getPincodeDetails("560001");
        verifyNoInteractions(countryRepository);
    }

    // ==================== enrichAddressWithDisplayNames ====================

    @Test
    void enrichAddressWithDisplayNames_nullInput_returnsNull() {
        AddressData result = addressDataService.enrichAddressWithDisplayNames(null);
        assertNull(result);
    }

    @Test
    void enrichAddressWithDisplayNames_noLocationCodes_returnsUnmodifiedCopy() {
        AddressData input = AddressData.builder()
                .id("addr-1")
                .address("123 Street")
                .pincode("560001")
                .addressType(AddressType.CURRENT)
                .build();

        AddressData result = addressDataService.enrichAddressWithDisplayNames(input);

        assertNotNull(result);
        assertEquals("addr-1", result.getId());
        assertEquals("123 Street", result.getAddress());
        assertEquals("560001", result.getPincode());
        assertEquals(AddressType.CURRENT, result.getAddressType());
        verifyNoInteractions(locationRepository);
    }

    @Test
    void enrichAddressWithDisplayNames_withLocationCodes_enrichesNames() {
        AddressData input = AddressData.builder()
                .id("addr-1")
                .countryCode("IN")
                .stateCode("KA")
                .regionCode("REG01")
                .districtCode("DIST01")
                .talukaCode("TAL01")
                .villageCode("VIL01")
                .district("Old District")
                .build();

        LocationDisplayNames displayNames = LocationDisplayNames.builder()
                .countryName("India")
                .stateName("Karnataka")
                .regionValue(new MasterLanguageData("North Karnataka", null))
                .districtValue(new MasterLanguageData("Bangalore Urban", null))
                .talukaValue(new MasterLanguageData("Bangalore South", null))
                .villageValue(new MasterLanguageData("Test Village", null))
                .build();

        when(locationRepository.findDisplayNamesByCodes("IN", "KA", "REG01", "DIST01", "TAL01", "VIL01", null))
                .thenReturn(Optional.of(displayNames));

        AddressData result = addressDataService.enrichAddressWithDisplayNames(input);

        assertNotNull(result);
        assertEquals("India", result.getCountry());
        assertEquals("Karnataka", result.getState());
        assertEquals("North Karnataka", result.getRegion());
        assertNotNull(result.getDistrict());
        assertNotNull(result.getTaluka());
        assertNotNull(result.getVillageName());
        assertEquals("IN", result.getCountryCode());
        assertEquals("REG01", result.getRegionCode());
    }

    @Test
    void enrichAddressWithDisplayNames_locationRepoReturnsEmpty_keepsOriginalValues() {
        AddressData input = AddressData.builder()
                .countryCode("IN")
                .country("Original Country")
                .state("Original State")
                .build();

        when(locationRepository.findDisplayNamesByCodes(eq("IN"), any(), any(), any(), any(), any(), any()))
                .thenReturn(Optional.empty());

        AddressData result = addressDataService.enrichAddressWithDisplayNames(input);

        assertEquals("Original Country", result.getCountry());
        assertEquals("Original State", result.getState());
    }

    @Test
    void enrichAddressWithDisplayNames_partialNamesReturned_onlyOverridesNonNull() {
        AddressData input = AddressData.builder()
                .countryCode("IN")
                .stateCode("KA")
                .country("Old Country")
                .state("Old State")
                .district("Old District")
                .build();

        LocationDisplayNames displayNames = LocationDisplayNames.builder()
                .countryName("India")
                .stateName(null)
                .districtValue(null)
                .talukaValue(null)
                .villageValue(null)
                .build();

        when(locationRepository.findDisplayNamesByCodes("IN", "KA", null, null, null, null, null))
                .thenReturn(Optional.of(displayNames));

        AddressData result = addressDataService.enrichAddressWithDisplayNames(input);

        assertEquals("India", result.getCountry());
        assertEquals("Old State", result.getState());
        assertEquals("Old District", result.getDistrict());
    }

    @Test
    void enrichAddressWithDisplayNames_doesNotMutateInput() {
        AddressData input = AddressData.builder()
                .id("addr-1")
                .countryCode("IN")
                .country("Original")
                .build();

        LocationDisplayNames displayNames = LocationDisplayNames.builder()
                .countryName("India")
                .build();

        when(locationRepository.findDisplayNamesByCodes(eq("IN"), any(), any(), any(), any(), any(), any()))
                .thenReturn(Optional.of(displayNames));

        AddressData result = addressDataService.enrichAddressWithDisplayNames(input);

        assertEquals("Original", input.getCountry());
        assertEquals("India", result.getCountry());
        assertNotSame(input, result);
    }

    @Test
    void enrichAddressWithDisplayNames_copiesAllFields() {
        AddressData input = AddressData.builder()
                .id("addr-1")
                .addressType(AddressType.CURRENT)
                .address("123 Street")
                .pincode("560001")
                .district("Dist")
                .country("Country")
                .state("State")
                .region("North Karnataka")
                .taluka("Taluka")
                .districtCode("DC")
                .regionCode("RC")
                .stateCode("SC")
                .countryCode("CC")
                .talukaCode("TC")
                .districtId(10L)
                .regionId(50L)
                .stateId(5L)
                .countryId(1L)
                .talukaId(20L)
                .villageCode("VC")
                .villageId(100L)
                .villageName("Village")
                .operatingAreaName("North Zone")
                .operatingAreaCode("OA01")
                .operatingAreaId(200L)
                .isServiceable(true)
                .build();

        when(locationRepository.findDisplayNamesByCodes("CC", "SC", "RC", "DC", "TC", "VC", "OA01"))
                .thenReturn(Optional.empty());

        AddressData result = addressDataService.enrichAddressWithDisplayNames(input);

        assertEquals("addr-1", result.getId());
        assertEquals(AddressType.CURRENT, result.getAddressType());
        assertEquals("123 Street", result.getAddress());
        assertEquals("560001", result.getPincode());
        assertEquals("North Karnataka", result.getRegion());
        assertEquals("RC", result.getRegionCode());
        assertEquals(50L, result.getRegionId());
        assertEquals(10L, result.getDistrictId());
        assertEquals(5L, result.getStateId());
        assertEquals(1L, result.getCountryId());
        assertEquals(20L, result.getTalukaId());
        assertEquals("VC", result.getVillageCode());
        assertEquals(100L, result.getVillageId());
        assertEquals("Village", result.getVillageName());
        assertEquals("North Zone", result.getOperatingAreaName());
        assertEquals("OA01", result.getOperatingAreaCode());
        assertEquals(200L, result.getOperatingAreaId());
        assertTrue(result.getIsServiceable());
    }
}
