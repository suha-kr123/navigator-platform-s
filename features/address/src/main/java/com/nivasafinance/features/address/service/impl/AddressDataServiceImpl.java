package com.nivasafinance.features.address.service.impl;

import com.nivasafinance.common.base.model.MasterLanguageResolver;
import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.AddressRequest;
import com.nivasafinance.features.address.service.AddressDataService;
import com.nivasafinance.features.master.location.entity.*;
import com.nivasafinance.features.master.location.repository.*;
import com.nivasafinance.features.master.pincode.dto.PincodeResponse;
import com.nivasafinance.features.master.pincode.service.PincodeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementation of AddressDataService that provides common address creation logic.
 * This implementation follows the address creation pattern used in the lead module.
 */
@Service
@AllArgsConstructor
@Slf4j
public class AddressDataServiceImpl implements AddressDataService {

    private final PincodeService pincodeService;
    private final LocationRepository locationRepository;
    private final CountryRepository countryRepository;
    private final StateRepository stateRepository;
    private final DistrictRepository districtRepository;
    private final TalukaRepository talukaRepository;
    private final VillageRepository villageRepository;

    @Override
    public AddressData createAddressData(AddressRequest addressRequest) {
        // Create AddressData with basic information
        AddressData addressData = new AddressData();
        addressData.setAddress(addressRequest.getAddress());
        
        // Initialize with default values
        addressData.setCountry(null);
        addressData.setState(null);
        addressData.setDistrict(null);
        addressData.setTaluka(null);
        addressData.setIsServiceable(false);
        
        // Priority 1: If pincode is provided, use pincode-based lookup
        if (addressRequest.getPincode() != null && 
            addressRequest.getPincode().getPincode() != null && 
            !addressRequest.getPincode().getPincode().trim().isEmpty()) {
            
            addressData.setPincode(addressRequest.getPincode().getPincode());
            
            try {
                PincodeResponse pincodeResponse = pincodeService.getPincodeDetails(
                    addressRequest.getPincode().getPincode());
                
                // Populate address data from pincode response
                populateAddressDataFromPincodeResponse(addressData, pincodeResponse);
                
                // Handle village from pincode request
                handleVillageFromPincodeRequest(addressData, addressRequest.getPincode(), 
                    pincodeResponse.getTalukaId());
                
            } catch (Exception e) {
                log.warn("Failed to fetch pincode details for pincode: {}. Continuing with null values.",
                        addressRequest.getPincode().getPincode(), e);
            }
        }
        // Priority 2: If location codes are provided, use location code-based lookup
        else if (addressRequest.getLocation() != null) {
            try {
                populateAddressDataFromLocationCodes(addressData, addressRequest.getLocation());
            } catch (Exception e) {
                log.warn("Failed to fetch location details from codes. Continuing with null values.", e);
            }
        }

        return addressData;
    }

    @Override
    public AddressData enrichAddressWithDisplayNames(AddressData address) {
        if (address == null) {
            return null;
        }
        AddressData copy = AddressData.builder()
                .id(address.getId())
                .addressType(address.getAddressType())
                .address(address.getAddress())
                .pincode(address.getPincode())
                .district(address.getDistrict())
                .country(address.getCountry())
                .state(address.getState())
                .region(address.getRegion())
                .taluka(address.getTaluka())
                .districtCode(address.getDistrictCode())
                .regionCode(address.getRegionCode())
                .stateCode(address.getStateCode())
                .countryCode(address.getCountryCode())
                .talukaCode(address.getTalukaCode())
                .districtId(address.getDistrictId())
                .regionId(address.getRegionId())
                .stateId(address.getStateId())
                .countryId(address.getCountryId())
                .talukaId(address.getTalukaId())
                .villageCode(address.getVillageCode())
                .villageId(address.getVillageId())
                .villageName(address.getVillageName())
                .operatingAreaName(address.getOperatingAreaName())
                .operatingAreaCode(address.getOperatingAreaCode())
                .operatingAreaId(address.getOperatingAreaId())
                .isServiceable(address.getIsServiceable())
                .build();
        enrichNamesFromLocationMasters(copy);
        return copy;
    }

    private void enrichNamesFromLocationMasters(AddressData a) {
        if (!hasAnyLocationCode(a)) {
            return;
        }
        locationRepository.findDisplayNamesByCodes(
                a.getCountryCode(), a.getStateCode(), a.getRegionCode(), a.getDistrictCode(), a.getTalukaCode(), a.getVillageCode(), a.getOperatingAreaCode()
        ).ifPresent(names -> {
            if (names.getCountryName() != null) a.setCountry(names.getCountryName());
            if (names.getStateName() != null) a.setState(names.getStateName());
            if (names.getRegionValue() != null) a.setRegion(MasterLanguageResolver.getDisplayValue(names.getRegionValue()));
            if (names.getDistrictValue() != null) a.setDistrict(MasterLanguageResolver.getDisplayValue(names.getDistrictValue()));
            if (names.getTalukaValue() != null) a.setTaluka(MasterLanguageResolver.getDisplayValue(names.getTalukaValue()));
            if (names.getVillageValue() != null) a.setVillageName(MasterLanguageResolver.getDisplayValue(names.getVillageValue()));
            if (names.getOperatingAreaValue() != null) a.setOperatingAreaName(MasterLanguageResolver.getDisplayValue(names.getOperatingAreaValue()));
        });
    }

    private static boolean hasAnyLocationCode(AddressData a) {
        return isNotBlank(a.getCountryCode()) || isNotBlank(a.getStateCode()) || isNotBlank(a.getRegionCode())
                || isNotBlank(a.getDistrictCode()) || isNotBlank(a.getTalukaCode()) || isNotBlank(a.getVillageCode())
                || isNotBlank(a.getOperatingAreaCode());
    }

    private static boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }

    private void populateAddressDataFromPincodeResponse(AddressData addressData, PincodeResponse pincodeResponse) {
        addressData.setDistrict(pincodeResponse.getDistrict());
        addressData.setState(pincodeResponse.getState());
        addressData.setCountry(pincodeResponse.getCountry());
        addressData.setTaluka(pincodeResponse.getTaluka());
        addressData.setDistrictCode(pincodeResponse.getDistrictCode());
        addressData.setStateCode(pincodeResponse.getStateCode());
        addressData.setCountryCode(pincodeResponse.getCountryCode());
        addressData.setTalukaCode(pincodeResponse.getTalukaCode());
        addressData.setDistrictId(pincodeResponse.getDistrictId());
        addressData.setStateId(pincodeResponse.getStateId());
        addressData.setCountryId(pincodeResponse.getCountryId());
        addressData.setTalukaId(pincodeResponse.getTalukaId());
        addressData.setIsServiceable(pincodeResponse.getIsServicable());
    }
    
    private void handleVillageFromPincodeRequest(AddressData addressData, 
                                                  AddressRequest.PincodeRequest pincodeRequest, 
                                                  Long talukaId) {
        if (pincodeRequest.getVillageCode() != null && talukaId != null) {
            // Village selected from master list
            try {
                java.util.Optional<Village> villageOpt = villageRepository.findByCodeAndTalukaIdAndIsActiveTrue(
                        pincodeRequest.getVillageCode(), talukaId);
                if (villageOpt.isPresent()) {
                    Village village = villageOpt.get();
                    addressData.setVillageCode(village.getCode());
                    addressData.setVillageId(village.getId());
                    addressData.setVillageName(MasterLanguageResolver.getDisplayValue(village.getNameValues()));
                }
            } catch (Exception e) {
                log.warn("Failed to fetch village by code: {} for taluka: {}. Continuing with null values.",
                        pincodeRequest.getVillageCode(), talukaId, e);
            }
        } else if (pincodeRequest.getVillageName() != null && 
                   !pincodeRequest.getVillageName().trim().isEmpty()) {
            // Free text village name provided
            addressData.setVillageName(pincodeRequest.getVillageName());
        }
    }
    
    private void populateAddressDataFromLocationCodes(AddressData addressData, 
                                                      AddressRequest.AddressLocationRequest location) {
        Long countryId = null;
        Long stateId = null;
        Long districtId = null;
        Long talukaId = null;
        
        // Lookup Country
        if (location.getCountryCode() != null && !location.getCountryCode().trim().isEmpty()) {
            try {
                java.util.Optional<Country> countryOpt = countryRepository.findByCodeAndIsActiveTrue(
                        location.getCountryCode());
                if (countryOpt.isPresent()) {
                    Country country = countryOpt.get();
                    addressData.setCountryCode(country.getCode());
                    addressData.setCountryId(country.getId());
                    addressData.setCountry(country.getName());
                    countryId = country.getId();
                }
            } catch (Exception e) {
                log.warn("Failed to fetch country by code: {}", location.getCountryCode(), e);
            }
        }
        
        // Lookup State (requires countryId)
        if (location.getStateCode() != null && !location.getStateCode().trim().isEmpty() && countryId != null) {
            try {
                java.util.Optional<State> stateOpt = stateRepository.findByCodeAndCountryIdAndIsActiveTrue(
                        location.getStateCode(), countryId);
                if (stateOpt.isPresent()) {
                    State state = stateOpt.get();
                    addressData.setStateCode(state.getCode());
                    addressData.setStateId(state.getId());
                    addressData.setState(state.getName());
                    stateId = state.getId();
                }
            } catch (Exception e) {
                log.warn("Failed to fetch state by code: {} for country: {}", 
                        location.getStateCode(), countryId, e);
            }
        }
        
        // Lookup District (requires stateId)
        if (location.getDistrictCode() != null && !location.getDistrictCode().trim().isEmpty() && stateId != null) {
            try {
                java.util.Optional<District> districtOpt = districtRepository.findByCodeAndStateIdAndIsActiveTrue(
                        location.getDistrictCode(), stateId);
                if (districtOpt.isPresent()) {
                    District district = districtOpt.get();
                    addressData.setDistrictCode(district.getCode());
                    addressData.setDistrictId(district.getId());
                    addressData.setDistrict(MasterLanguageResolver.getDisplayValue(district.getNameValues()));
                    districtId = district.getId();
                }
            } catch (Exception e) {
                log.warn("Failed to fetch district by code: {} for state: {}", 
                        location.getDistrictCode(), stateId, e);
            }
        }
        
        // Lookup Taluka (requires districtId)
        if (location.getTalukaCode() != null && !location.getTalukaCode().trim().isEmpty() && districtId != null) {
            try {
                java.util.Optional<Taluka> talukaOpt = talukaRepository.findByCodeAndDistrictIdAndIsActiveTrue(
                        location.getTalukaCode(), districtId);
                if (talukaOpt.isPresent()) {
                    Taluka taluka = talukaOpt.get();
                    addressData.setTalukaCode(taluka.getCode());
                    addressData.setTalukaId(taluka.getId());
                    addressData.setTaluka(MasterLanguageResolver.getDisplayValue(taluka.getNameValues()));
                    talukaId = taluka.getId();
                }
            } catch (Exception e) {
                log.warn("Failed to fetch taluka by code: {} for district: {}", 
                        location.getTalukaCode(), districtId, e);
            }
        }
        
        // Handle village from location request
        if (location.getVillageCode() != null && talukaId != null) {
            // Village selected from master list
            try {
                java.util.Optional<Village> villageOpt = villageRepository.findByCodeAndTalukaIdAndIsActiveTrue(
                        location.getVillageCode(), talukaId);
                if (villageOpt.isPresent()) {
                    Village village = villageOpt.get();
                    addressData.setVillageCode(village.getCode());
                    addressData.setVillageId(village.getId());
                    addressData.setVillageName(MasterLanguageResolver.getDisplayValue(village.getNameValues()));
                }
            } catch (Exception e) {
                log.warn("Failed to fetch village by code: {} for taluka: {}. Continuing with null values.",
                        location.getVillageCode(), talukaId, e);
            }
        } else if (location.getVillageName() != null && !location.getVillageName().trim().isEmpty()) {
            // Free text village name provided
            addressData.setVillageName(location.getVillageName());
        }
    }
}

