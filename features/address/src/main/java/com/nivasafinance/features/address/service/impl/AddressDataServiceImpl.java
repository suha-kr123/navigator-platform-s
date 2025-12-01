package com.nivasafinance.features.address.service.impl;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.AddressRequest;
import com.nivasafinance.features.address.service.AddressDataService;
import com.nivasafinance.features.master.location.entity.Village;
import com.nivasafinance.features.master.location.repository.VillageRepository;
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
    private final VillageRepository villageRepository;

    @Override
    public AddressData createAddressData(AddressRequest addressRequest) {
        // Create AddressData with basic information
        AddressData addressData = new AddressData();
        addressData.setAddress(addressRequest.getAddress());
        addressData.setPincode(addressRequest.getPincode());
        
        // Initialize with default values
        addressData.setCountry(null);
        addressData.setState(null);
        addressData.setDistrict(null);
        addressData.setIsServiceable(false);

        // Try to fetch pincode data and enrich address
        try {
            PincodeResponse pincodeResponse = pincodeService.getPincodeDetails(addressRequest.getPincode());
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
            
            // Handle village - either from master list (villageCode) or free text (villageName)
            if (addressRequest.getVillageCode() != null && pincodeResponse.getTalukaId() != null) {
                // Village selected from master list
                try {
                    java.util.Optional<Village> villageOpt = villageRepository.findByCodeAndTalukaIdAndIsActiveTrue(
                            addressRequest.getVillageCode(), pincodeResponse.getTalukaId());
                    if (villageOpt.isPresent()) {
                        Village village = villageOpt.get();
                        addressData.setVillageCode(village.getCode());
                        addressData.setVillageId(village.getId());
                        addressData.setVillageName(village.getName());
                    }
                } catch (Exception e) {
                    log.warn("Failed to fetch village by code: {} for taluka: {}. Continuing with null values.",
                            addressRequest.getVillageCode(), pincodeResponse.getTalukaId(), e);
                }
            } else if (addressRequest.getVillageName() != null && !addressRequest.getVillageName().trim().isEmpty()) {
                // Free text village name provided
                addressData.setVillageName(addressRequest.getVillageName());
            }
        } catch (Exception e) {
            log.warn("Failed to fetch pincode details for pincode: {}. Continuing with null values.",
                    addressRequest.getPincode(), e);
        }

        return addressData;
    }
}

