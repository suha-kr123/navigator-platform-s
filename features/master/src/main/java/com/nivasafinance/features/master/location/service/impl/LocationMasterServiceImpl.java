package com.nivasafinance.features.master.location.service.impl;

import com.nivasafinance.common.base.model.MasterLanguageResolver;
import com.nivasafinance.features.master.location.dto.*;
import com.nivasafinance.features.master.location.entity.*;
import com.nivasafinance.features.master.location.exception.LocationNotFoundException;
import com.nivasafinance.features.master.location.repository.*;
import com.nivasafinance.features.master.location.service.LocationMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LocationMasterServiceImpl implements LocationMasterService {

    private final CountryRepository countryRepository;
    private final StateRepository stateRepository;
    private final RegionRepository regionRepository;
    private final DistrictRepository districtRepository;
    private final TalukaRepository talukaRepository;
    private final VillageRepository villageRepository;
    private final MessageSource messageSource;

    @Override
    public List<CountryResponse> getAllCountries() {
        List<Country> countries = countryRepository.findAllByIsActiveTrue();
        return countries.stream()
                .map(this::mapToCountryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<StateResponse> getStatesByCountryId(Long countryId) {
        // Validate country exists
        countryRepository.findById(countryId)
                .orElseThrow(() -> new LocationNotFoundException("Country", countryId, messageSource));

        List<State> states = stateRepository.findByCountryIdAndIsActiveTrue(countryId);
        return states.stream()
                .map(this::mapToStateResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RegionResponse> getRegionsByStateId(Long stateId) {
        // Validate state exists
        stateRepository.findById(stateId)
                .orElseThrow(() -> new LocationNotFoundException("State", stateId, messageSource));
        List<Region> regions = regionRepository.findByStateIdAndIsActiveTrue(stateId);
        return regions.stream()
                .map(this::mapToRegionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DistrictResponse> getDistrictsByRegionId(Long regionId) {
        //validate region exists
        regionRepository.findById(regionId)
                .orElseThrow(() -> new LocationNotFoundException("Region", regionId, messageSource));

        List<District> districts = districtRepository.findByRegionIdAndIsActiveTrue(regionId);
        return districts.stream()
                .map(this::mapToDistrictResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DistrictResponse> getDistrictsByStateId(Long stateId) {
        // Validate state exists
        stateRepository.findById(stateId)
                .orElseThrow(() -> new LocationNotFoundException("State", stateId, messageSource));

        List<District> districts = districtRepository.findByStateIdAndIsActiveTrue(stateId);
        return districts.stream()
                .map(this::mapToDistrictResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TalukaResponse> getTalukasByDistrictId(Long districtId) {
        // Validate district exists
        districtRepository.findById(districtId)
                .orElseThrow(() -> new LocationNotFoundException("District", districtId, messageSource));

        List<Taluka> talukas = talukaRepository.findByDistrictIdAndIsActiveTrue(districtId);
        return talukas.stream()
                .map(this::mapToTalukaResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<VillageResponse> getVillagesByTalukaId(Long talukaId) {
        // Validate taluka exists
        talukaRepository.findById(talukaId)
                .orElseThrow(() -> new LocationNotFoundException("Taluka", talukaId, messageSource));

        List<Village> villages = villageRepository.findByTalukaIdAndIsActiveTrue(talukaId);
        return villages.stream()
                .map(this::mapToVillageResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DistrictResponse> getServiceableDistrictsByStateId(Long stateId) {
        stateRepository.findById(stateId)
                .orElseThrow(() -> new LocationNotFoundException("State", stateId, messageSource));
        List<District> districts = districtRepository.findServiceableDistrictsByStateId(stateId);
        return districts.stream()
                .map(this::mapToDistrictResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TalukaResponse> getServiceableTalukasByDistrictId(Long districtId) {
        districtRepository.findById(districtId)
                .orElseThrow(() -> new LocationNotFoundException("District", districtId, messageSource));
        List<Taluka> talukas = talukaRepository.findServiceableTalukasByDistrictId(districtId);
        return talukas.stream()
                .map(this::mapToTalukaResponse)
                .collect(Collectors.toList());
    }

    private CountryResponse mapToCountryResponse(Country country) {
        return CountryResponse.builder()
                .id(country.getId())
                .name(country.getName())
                .code(country.getCode())
                .isActive(country.getIsActive())
                .build();
    }

    private StateResponse mapToStateResponse(State state) {
        return StateResponse.builder()
                .id(state.getId())
                .name(state.getName())
                .code(state.getCode())
                .isActive(state.getIsActive())
                .build();
    }

    private RegionResponse mapToRegionResponse(Region region) {
        return RegionResponse.builder()
                .id(region.getId())
                .name(MasterLanguageResolver.getDisplayValue(region.getName()))
                .code(region.getCode())
                .isActive(region.getIsActive())
                .displayOrder(region.getDisplayOrder())
                .build();
    }

    private DistrictResponse mapToDistrictResponse(District district) {
        return DistrictResponse.builder()
                .id(district.getId())
                .name(MasterLanguageResolver.getDisplayValue(district.getNameValues()))
                .code(district.getCode())
                .isActive(district.getIsActive())
                .displayOrder(district.getDisplayOrder())
                .build();
    }

    private TalukaResponse mapToTalukaResponse(Taluka taluka) {
        return TalukaResponse.builder()
                .id(taluka.getId())
                .name(MasterLanguageResolver.getDisplayValue(taluka.getNameValues()))
                .code(taluka.getCode())
                .isActive(taluka.getIsActive())
                .displayOrder(taluka.getDisplayOrder())
                .build();
    }

    private VillageResponse mapToVillageResponse(Village village) {
        return VillageResponse.builder()
                .id(village.getId())
                .name(MasterLanguageResolver.getDisplayValue(village.getNameValues()))
                .code(village.getCode())
                .isActive(village.getIsActive())
                .build();
    }
}

