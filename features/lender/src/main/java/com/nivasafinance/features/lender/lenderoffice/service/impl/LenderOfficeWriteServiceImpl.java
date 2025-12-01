package com.nivasafinance.features.lender.lenderoffice.service.impl;

import com.nivasafinance.features.address.service.AddressDataService;
import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeReponseData;
import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeRequestData;
import com.nivasafinance.features.lender.lenderoffice.entity.LenderOffice;
import com.nivasafinance.features.lender.lenderoffice.repository.LenderOfficeRepositoryWrapper;
import com.nivasafinance.features.lender.lenderoffice.service.LenderOfficeWriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class LenderOfficeWriteServiceImpl implements LenderOfficeWriteService {

    private final AddressDataService addressDataService;
    private final LenderOfficeRepositoryWrapper lenderOfficeRepositoryWrapper;

    @Autowired
    public LenderOfficeWriteServiceImpl(
            AddressDataService addressDataService,
            LenderOfficeRepositoryWrapper lenderOfficeRepositoryWrapper) {
        this.addressDataService = addressDataService;
        this.lenderOfficeRepositoryWrapper = lenderOfficeRepositoryWrapper;
    }

    @Override
    public LenderOfficeReponseData create(LenderOfficeRequestData lenderOfficeData) {
        // Use common address data service for address creation
        com.nivasafinance.common.dto.AddressData addressData = null;
        if (lenderOfficeData.getCreateAddressRequest() != null) {
            addressData = addressDataService.createAddressData(lenderOfficeData.getCreateAddressRequest());
        }

        LenderOffice lenderOffice = new LenderOffice();
        lenderOffice.setName(lenderOfficeData.getName());
        lenderOffice.setKey(lenderOfficeData.getKey());
        lenderOffice.setLenderKey(lenderOfficeData.getLenderKey());
        
        // Wrap AddressData in nested structure (like Lead does)
        if (addressData != null) {
            LenderOffice.AddressDetails addressDetails = LenderOffice.AddressDetails.builder()
                    .address(addressData)
                    .build();
            lenderOffice.setAddressDetails(addressDetails);
        }
        
        lenderOffice.setStatus(lenderOfficeData.getStatus());
        
        LenderOffice savedLenderOffice = lenderOfficeRepositoryWrapper.saveWithException(lenderOffice);
        return toResponse(savedLenderOffice);
    }

    @Override
    public LenderOfficeReponseData update(UUID id, LenderOfficeRequestData lenderOfficeData) {
        LenderOffice existingLenderOffice = lenderOfficeRepositoryWrapper.findByIdWithException(id);

        // Use common address data service for address creation
        com.nivasafinance.common.dto.AddressData addressData = null;
        if (lenderOfficeData.getCreateAddressRequest() != null) {
            addressData = addressDataService.createAddressData(lenderOfficeData.getCreateAddressRequest());
        }

        existingLenderOffice.setName(lenderOfficeData.getName());
        existingLenderOffice.setKey(lenderOfficeData.getKey());
        existingLenderOffice.setLenderKey(lenderOfficeData.getLenderKey());
        
        // Wrap AddressData in nested structure (like Lead does)
        if (addressData != null) {
            LenderOffice.AddressDetails addressDetails = LenderOffice.AddressDetails.builder()
                    .address(addressData)
                    .build();
            existingLenderOffice.setAddressDetails(addressDetails);
        } else {
            existingLenderOffice.setAddressDetails(null);
        }
        
        existingLenderOffice.setStatus(lenderOfficeData.getStatus());
        
        LenderOffice savedLenderOffice = lenderOfficeRepositoryWrapper.saveWithException(existingLenderOffice);
        return toResponse(savedLenderOffice);
    }

    @Override
    public void delete(UUID id) {
        lenderOfficeRepositoryWrapper.deleteByIdWithException(id);
    }

    private LenderOfficeReponseData toResponse(LenderOffice lenderOffice) {
        if (lenderOffice.getId() == null) {
            throw new IllegalStateException("Lender office ID cannot be null");
        }
        // Unwrap AddressData from nested structure
        com.nivasafinance.common.dto.AddressData addressData = null;
        if (lenderOffice.getAddressDetails() != null) {
            addressData = lenderOffice.getAddressDetails().getAddress();
        }
        return new LenderOfficeReponseData(
                lenderOffice.getId(),
                lenderOffice.getName(),
                lenderOffice.getKey(),
                lenderOffice.getLenderKey(),
                addressData
        );
    }
}

