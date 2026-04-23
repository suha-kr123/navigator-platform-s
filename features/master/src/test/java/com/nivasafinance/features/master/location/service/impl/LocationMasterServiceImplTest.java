package com.nivasafinance.features.master.location.service.impl;

import com.nivasafinance.common.base.model.MasterLanguageData;
import com.nivasafinance.features.master.location.dto.*;
import com.nivasafinance.features.master.location.entity.*;
import com.nivasafinance.features.master.location.exception.LocationNotFoundException;
import com.nivasafinance.features.master.location.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationMasterServiceImplTest {

    @Mock
    private CountryRepository countryRepository;

    @Mock
    private StateRepository stateRepository;

    @Mock
    private RegionRepository regionRepository;

    @Mock
    private DistrictRepository districtRepository;

    @Mock
    private TalukaRepository talukaRepository;

    @Mock
    private VillageRepository villageRepository;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private LocationMasterServiceImpl locationMasterService;

    private Country country;
    private State state;
    private Region region;
    private District district;
    private Taluka taluka;
    private Village village;

    @BeforeEach
    void setUp() {
        country = new Country();
        country.setId(1L);
        country.setName("India");
        country.setCode("IN");
        country.setIsActive(true);

        state = new State();
        state.setId(10L);
        state.setCountryId(1L);
        state.setName("Karnataka");
        state.setCode("KA");
        state.setIsActive(true);

        region = new Region();
        region.setId(50L);
        region.setStateId(10L);
        region.setCode("REG01");
        region.setName(MasterLanguageData.builder().defaultValue("North Karnataka").build());
        region.setIsActive(true);
        region.setDisplayOrder(1);

        district = new District();
        district.setId(100L);
        district.setStateId(10L);
        district.setName("Bangalore Urban");
        district.setCode("BLR");
        district.setNameValues(MasterLanguageData.builder().defaultValue("Bangalore Urban").build());
        district.setIsActive(true);
        district.setDisplayOrder(1);

        taluka = new Taluka();
        taluka.setId(1000L);
        taluka.setDistrictId(100L);
        taluka.setName("Bangalore North");
        taluka.setCode("BN");
        taluka.setNameValues(MasterLanguageData.builder().defaultValue("Bangalore North").build());
        taluka.setIsActive(true);
        taluka.setDisplayOrder(1);

        village = new Village();
        village.setId(10000L);
        village.setTalukaId(1000L);
        village.setName("Yelahanka");
        village.setCode("YLK");
        village.setNameValues(MasterLanguageData.builder().defaultValue("Yelahanka").build());
        village.setIsActive(true);
    }

    // ── getAllCountries ───────────────────────────────────────────────

    @Test
    void getAllCountries_countriesExist_returnsCountryList() {
        when(countryRepository.findAllByIsActiveTrue()).thenReturn(List.of(country));

        List<CountryResponse> result = locationMasterService.getAllCountries();

        assertEquals(1, result.size(), "Should return one active country");
        assertEquals("India", result.get(0).getName(), "Country name should match");
        assertEquals("IN", result.get(0).getCode(), "Country code should match");
    }

    @Test
    void getAllCountries_noCountries_returnsEmptyList() {
        when(countryRepository.findAllByIsActiveTrue()).thenReturn(Collections.emptyList());

        List<CountryResponse> result = locationMasterService.getAllCountries();

        assertTrue(result.isEmpty(), "Should return empty list when no active countries exist");
    }

    // ── getStatesByCountryId ─────────────────────────────────────────

    @Test
    void getStatesByCountryId_validCountry_returnsStateList() {
        when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
        when(stateRepository.findByCountryIdAndIsActiveTrue(1L)).thenReturn(List.of(state));

        List<StateResponse> result = locationMasterService.getStatesByCountryId(1L);

        assertEquals(1, result.size(), "Should return one state for the country");
        assertEquals("Karnataka", result.get(0).getName(), "State name should match");
    }

    @Test
    void getStatesByCountryId_countryNotFound_throwsLocationNotFound() {
        when(countryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(LocationNotFoundException.class,
                () -> locationMasterService.getStatesByCountryId(999L),
                "Non-existent country ID should throw LocationNotFoundException");
    }

    @Test
    void getStatesByCountryId_noStates_returnsEmptyList() {
        when(countryRepository.findById(1L)).thenReturn(Optional.of(country));
        when(stateRepository.findByCountryIdAndIsActiveTrue(1L)).thenReturn(Collections.emptyList());

        List<StateResponse> result = locationMasterService.getStatesByCountryId(1L);

        assertTrue(result.isEmpty(), "Should return empty list when country has no active states");
    }

    // ── getDistrictsByStateId ────────────────────────────────────────

    @Test
    void getDistrictsByStateId_validState_returnsDistrictList() {
        when(stateRepository.findById(10L)).thenReturn(Optional.of(state));
        when(districtRepository.findByStateIdAndIsActiveTrue(10L)).thenReturn(List.of(district));

        List<DistrictResponse> result = locationMasterService.getDistrictsByStateId(10L);

        assertEquals(1, result.size(), "Should return one district for the state");
        assertEquals("BLR", result.get(0).getCode(), "District code should match");
    }

    @Test
    void getDistrictsByStateId_stateNotFound_throwsLocationNotFound() {
        when(stateRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(LocationNotFoundException.class,
                () -> locationMasterService.getDistrictsByStateId(999L),
                "Non-existent state ID should throw LocationNotFoundException");
    }

    @Test
    void getDistrictsByStateId_noDistricts_returnsEmptyList() {
        when(stateRepository.findById(10L)).thenReturn(Optional.of(state));
        when(districtRepository.findByStateIdAndIsActiveTrue(10L)).thenReturn(Collections.emptyList());

        List<DistrictResponse> result = locationMasterService.getDistrictsByStateId(10L);

        assertTrue(result.isEmpty(), "Should return empty list when state has no active districts");
    }

    // ── getTalukasByDistrictId ───────────────────────────────────────

    @Test
    void getTalukasByDistrictId_validDistrict_returnsTalukaList() {
        when(districtRepository.findById(100L)).thenReturn(Optional.of(district));
        when(talukaRepository.findByDistrictIdAndIsActiveTrue(100L)).thenReturn(List.of(taluka));

        List<TalukaResponse> result = locationMasterService.getTalukasByDistrictId(100L);

        assertEquals(1, result.size(), "Should return one taluka for the district");
        assertEquals("BN", result.get(0).getCode(), "Taluka code should match");
    }

    @Test
    void getTalukasByDistrictId_districtNotFound_throwsLocationNotFound() {
        when(districtRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(LocationNotFoundException.class,
                () -> locationMasterService.getTalukasByDistrictId(999L),
                "Non-existent district ID should throw LocationNotFoundException");
    }

    @Test
    void getTalukasByDistrictId_noTalukas_returnsEmptyList() {
        when(districtRepository.findById(100L)).thenReturn(Optional.of(district));
        when(talukaRepository.findByDistrictIdAndIsActiveTrue(100L)).thenReturn(Collections.emptyList());

        List<TalukaResponse> result = locationMasterService.getTalukasByDistrictId(100L);

        assertTrue(result.isEmpty(), "Should return empty list when district has no active talukas");
    }

    // ── getVillagesByTalukaId ────────────────────────────────────────

    @Test
    void getVillagesByTalukaId_validTaluka_returnsVillageList() {
        when(talukaRepository.findById(1000L)).thenReturn(Optional.of(taluka));
        when(villageRepository.findByTalukaIdAndIsActiveTrue(1000L)).thenReturn(List.of(village));

        List<VillageResponse> result = locationMasterService.getVillagesByTalukaId(1000L);

        assertEquals(1, result.size(), "Should return one village for the taluka");
        assertEquals("YLK", result.get(0).getCode(), "Village code should match");
    }

    @Test
    void getVillagesByTalukaId_talukaNotFound_throwsLocationNotFound() {
        when(talukaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(LocationNotFoundException.class,
                () -> locationMasterService.getVillagesByTalukaId(999L),
                "Non-existent taluka ID should throw LocationNotFoundException");
    }

    @Test
    void getVillagesByTalukaId_noVillages_returnsEmptyList() {
        when(talukaRepository.findById(1000L)).thenReturn(Optional.of(taluka));
        when(villageRepository.findByTalukaIdAndIsActiveTrue(1000L)).thenReturn(Collections.emptyList());

        List<VillageResponse> result = locationMasterService.getVillagesByTalukaId(1000L);

        assertTrue(result.isEmpty(), "Should return empty list when taluka has no active villages");
    }

    // ── getServiceableDistrictsByStateId ─────────────────────────────

    @Test
    void getServiceableDistrictsByStateId_validState_returnsServiceableDistricts() {
        when(stateRepository.findById(10L)).thenReturn(Optional.of(state));
        when(districtRepository.findServiceableDistrictsByStateId(10L)).thenReturn(List.of(district));

        List<DistrictResponse> result = locationMasterService.getServiceableDistrictsByStateId(10L);

        assertEquals(1, result.size(), "Should return one serviceable district");
        assertEquals("BLR", result.get(0).getCode(), "Serviceable district code should match");
    }

    @Test
    void getServiceableDistrictsByStateId_stateNotFound_throwsLocationNotFound() {
        when(stateRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(LocationNotFoundException.class,
                () -> locationMasterService.getServiceableDistrictsByStateId(999L),
                "Non-existent state ID should throw LocationNotFoundException");
    }

    @Test
    void getServiceableDistrictsByStateId_noServiceableDistricts_returnsEmptyList() {
        when(stateRepository.findById(10L)).thenReturn(Optional.of(state));
        when(districtRepository.findServiceableDistrictsByStateId(10L)).thenReturn(Collections.emptyList());

        List<DistrictResponse> result = locationMasterService.getServiceableDistrictsByStateId(10L);

        assertTrue(result.isEmpty(),
                "Should return empty list when no serviceable districts exist for the state");
    }

    // ── getServiceableTalukasByDistrictId ────────────────────────────

    @Test
    void getServiceableTalukasByDistrictId_validDistrict_returnsServiceableTalukas() {
        when(districtRepository.findById(100L)).thenReturn(Optional.of(district));
        when(talukaRepository.findServiceableTalukasByDistrictId(100L)).thenReturn(List.of(taluka));

        List<TalukaResponse> result = locationMasterService.getServiceableTalukasByDistrictId(100L);

        assertEquals(1, result.size(), "Should return one serviceable taluka");
        assertEquals("BN", result.get(0).getCode(), "Serviceable taluka code should match");
    }

    @Test
    void getServiceableTalukasByDistrictId_districtNotFound_throwsLocationNotFound() {
        when(districtRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(LocationNotFoundException.class,
                () -> locationMasterService.getServiceableTalukasByDistrictId(999L),
                "Non-existent district ID should throw LocationNotFoundException");
    }

    @Test
    void getServiceableTalukasByDistrictId_noServiceableTalukas_returnsEmptyList() {
        when(districtRepository.findById(100L)).thenReturn(Optional.of(district));
        when(talukaRepository.findServiceableTalukasByDistrictId(100L)).thenReturn(Collections.emptyList());

        List<TalukaResponse> result = locationMasterService.getServiceableTalukasByDistrictId(100L);

        assertTrue(result.isEmpty(),
                "Should return empty list when no serviceable talukas exist for the district");
    }

    // ── getRegionByStateId ───────────────────────────────────────────

    @Test
    void getRegionsByStateId_validState_returnsRegionList() {
        when(stateRepository.findById(10L)).thenReturn(Optional.of(state));
        when(regionRepository.findByStateIdAndIsActiveTrue(10L)).thenReturn(List.of(region));

        List<RegionResponse> result = locationMasterService.getRegionsByStateId(10L);

        assertEquals(1, result.size(), "Should return one region for the state");
        assertEquals("REG01", result.get(0).getCode(), "Region code should match");
    }

    @Test
    void getRegionsByStateId_stateNotFound_throwsLocationNotFound() {
        when(stateRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(LocationNotFoundException.class,
                () -> locationMasterService.getRegionsByStateId(999L),
                "Non-existent state ID should throw LocationNotFoundException");
    }

    @Test
    void getRegionsByStateId_noRegions_returnsEmptyList() {
        when(stateRepository.findById(10L)).thenReturn(Optional.of(state));
        when(regionRepository.findByStateIdAndIsActiveTrue(10L)).thenReturn(Collections.emptyList());

        List<RegionResponse> result = locationMasterService.getRegionsByStateId(10L);

        assertTrue(result.isEmpty(), "Should return empty list when state has no active regions");
    }

    @Test
    void getRegionsByStateId_responseFieldsCorrectlyMapped() {
        when(stateRepository.findById(10L)).thenReturn(Optional.of(state));
        when(regionRepository.findByStateIdAndIsActiveTrue(10L)).thenReturn(List.of(region));

        List<RegionResponse> result = locationMasterService.getRegionsByStateId(10L);

        RegionResponse response = result.get(0);
        assertEquals(50L, response.getId(), "Region ID should be mapped correctly");
        assertEquals("North Karnataka", response.getName(), "Region name should be resolved from MasterLanguageData");
        assertEquals("REG01", response.getCode(), "Region code should be mapped correctly");
        assertTrue(response.getIsActive(), "Region isActive should be mapped correctly");
        assertEquals(1, response.getDisplayOrder(), "Region displayOrder should be mapped correctly");
    }

    // ── getDistrictsByRegionId ───────────────────────────────────────

    @Test
    void getDistrictsByRegionId_validRegion_returnsDistrictList() {
        when(regionRepository.findById(50L)).thenReturn(Optional.of(region));
        when(districtRepository.findByRegionIdAndIsActiveTrue(50L)).thenReturn(List.of(district));

        List<DistrictResponse> result = locationMasterService.getDistrictsByRegionId(50L);

        assertEquals(1, result.size(), "Should return one district for the region");
        assertEquals("BLR", result.get(0).getCode(), "District code should match");
    }

    @Test
    void getDistrictsByRegionId_regionNotFound_throwsLocationNotFound() {
        when(regionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(LocationNotFoundException.class,
                () -> locationMasterService.getDistrictsByRegionId(999L),
                "Non-existent region ID should throw LocationNotFoundException");
    }

    @Test
    void getDistrictsByRegionId_noDistricts_returnsEmptyList() {
        when(regionRepository.findById(50L)).thenReturn(Optional.of(region));
        when(districtRepository.findByRegionIdAndIsActiveTrue(50L)).thenReturn(Collections.emptyList());

        List<DistrictResponse> result = locationMasterService.getDistrictsByRegionId(50L);

        assertTrue(result.isEmpty(), "Should return empty list when region has no active districts");
    }

    // ── response mapping ─────────────────────────────────────────────

    @Test
    void getAllCountries_responseFieldsCorrectlyMapped() {
        when(countryRepository.findAllByIsActiveTrue()).thenReturn(List.of(country));

        List<CountryResponse> result = locationMasterService.getAllCountries();

        CountryResponse response = result.get(0);
        assertEquals(1L, response.getId(), "Country ID should be mapped correctly");
        assertEquals("India", response.getName(), "Country name should be mapped correctly");
        assertEquals("IN", response.getCode(), "Country code should be mapped correctly");
        assertTrue(response.getIsActive(), "Country isActive should be mapped correctly");
    }

    @Test
    void getDistrictsByStateId_districtResponseIncludesDisplayOrder() {
        when(stateRepository.findById(10L)).thenReturn(Optional.of(state));
        when(districtRepository.findByStateIdAndIsActiveTrue(10L)).thenReturn(List.of(district));

        List<DistrictResponse> result = locationMasterService.getDistrictsByStateId(10L);

        assertEquals(1, result.get(0).getDisplayOrder(),
                "District display order should be mapped from entity");
    }

    @Test
    void getTalukasByDistrictId_talukaResponseIncludesDisplayOrder() {
        when(districtRepository.findById(100L)).thenReturn(Optional.of(district));
        when(talukaRepository.findByDistrictIdAndIsActiveTrue(100L)).thenReturn(List.of(taluka));

        List<TalukaResponse> result = locationMasterService.getTalukasByDistrictId(100L);

        assertEquals(1, result.get(0).getDisplayOrder(),
                "Taluka display order should be mapped from entity");
    }

    // ── isDistrictServiceable ────────────────────────────────────────

    @Test
    void isDistrictServiceable_whenDistrictCodeIsNull_returnsFalse() {
        boolean result = locationMasterService.isDistrictServiceable(null);

        assertFalse(result, "Null district code should not be serviceable");
        verifyNoInteractions(districtRepository);
    }

    @Test
    void isDistrictServiceable_whenDistrictCodeIsBlank_returnsFalse() {
        boolean result = locationMasterService.isDistrictServiceable("   ");

        assertFalse(result, "Blank district code should not be serviceable");
        verifyNoInteractions(districtRepository);
    }

    @Test
    void isDistrictServiceable_whenDistrictNotServiceable_returnsFalse() {
        when(districtRepository.existsServiceableDistrictByCode("BLR")).thenReturn(false);
        boolean result = locationMasterService.isDistrictServiceable("BLR");
        assertFalse(result, "Non-serviceable district should return false");
    }
    @Test
    void isDistrictServiceable_whenDistrictServiceable_returnsTrue() {
        when(districtRepository.existsServiceableDistrictByCode("BLR")).thenReturn(true);
        boolean result = locationMasterService.isDistrictServiceable("BLR");
        assertTrue(result, "Serviceable district should return true");
    }
}
