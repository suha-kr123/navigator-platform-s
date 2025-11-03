package com.nivasafinance.features.offices.service.impl;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.AddressRequest;
import com.nivasafinance.features.master.pincode.dto.PincodeResponse;
import com.nivasafinance.features.master.pincode.service.PincodeService;
import com.nivasafinance.features.offices.dto.OfficeCreateRequest;
import com.nivasafinance.features.offices.dto.OfficeResponse;
import com.nivasafinance.features.offices.entity.Office;
import com.nivasafinance.features.offices.repository.OfficeRepository;
import com.nivasafinance.features.offices.service.OfficeCodeFactory;
import com.nivasafinance.features.offices.service.OfficeWriteService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class OfficeWriteServiceImpl implements OfficeWriteService {

    private final OfficeRepository officeRepository;
    private final PincodeService pincodeService;
    private final OfficeCodeFactory officeCodeFactory;

    @Override
    @Transactional(noRollbackFor = Exception.class)
    public OfficeResponse createOffice(OfficeCreateRequest request) {
        AddressData addressData = createAddressData(request.getAddress());

        // Generate hierarchical code using factory
        String generatedCode = officeCodeFactory.generateOfficeCode(request.getParentId());

        // Create the office with address data embedded as JSONB
        Office office = new Office();
        office.setName(request.getName());
        office.setKey(request.getKey());
        office.setCode(generatedCode);
        office.setAddressData(addressData);
        office.setParentId(request.getParentId());

        Office savedOffice = officeRepository.save(office);
        return toResponse(savedOffice);
    }

    private AddressData createAddressData(AddressRequest addressRequest) {
        AddressData addressData = new AddressData();
        addressData.setAddressLineOne(addressRequest.getAddressLineOne());
        addressData.setAddressLineTwo(addressRequest.getAddressLineTwo());
        addressData.setPincode(addressRequest.getPincode());
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
            log.warn("Failed to fetch pincode details for pincode: {}. Continuing with provided values.",
                    addressRequest.getPincode(), e);
        }
        addressData.setArea(addressRequest.getArea());

        return addressData;
    }

    private OfficeResponse toResponse(Office entity) {
        return new OfficeResponse(
                entity.getId(),
                entity.getName(),
                entity.getKey(),
                entity.getCode(),
                entity.getAddressData(),
                entity.getParentId()
        );
    }
}

