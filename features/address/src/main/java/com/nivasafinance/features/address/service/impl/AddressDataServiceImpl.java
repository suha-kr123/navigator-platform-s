package com.nivasafinance.features.address.service.impl;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.AddressRequest;
import com.nivasafinance.features.address.service.AddressDataService;
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

    @Override
    public AddressData createAddressData(AddressRequest addressRequest) {
        // Create AddressData with basic information
        AddressData addressData = new AddressData();
        addressData.setAddressLineOne(addressRequest.getAddressLineOne());
        addressData.setAddressLineTwo(addressRequest.getAddressLineTwo());
        addressData.setPincode(addressRequest.getPincode());
        addressData.setArea(addressRequest.getArea());
        
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
            addressData.setIsServiceable(pincodeResponse.isServicable());
        } catch (Exception e) {
            log.warn("Failed to fetch pincode details for pincode: {}. Continuing with null values.",
                    addressRequest.getPincode(), e);
        }

        return addressData;
    }
}

