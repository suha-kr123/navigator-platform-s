package com.nivasafinance.features.address.service;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.AddressRequest;

/**
 * Service for creating and managing AddressData objects.
 * This service provides common address creation logic that can be used across multiple modules
 * (lead, office, lenderoffice, etc.) to avoid code duplication.
 */
public interface AddressDataService {
    
    /**
     * Creates an AddressData object from an AddressRequest.
     * This method enriches the address with pincode details (district, state, country, serviceability)
     * by fetching data from the PincodeService. If pincode lookup fails, it continues with
     * the provided values.
     * 
     * @param addressRequest the address request containing basic address information
     * @return AddressData object enriched with pincode details
     */
    AddressData createAddressData(AddressRequest addressRequest);
}

