package com.nivasafinance.features.lender.lenderoffice.service.impl;

import com.nivasafinance.features.address.service.AddressDataService;
import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeReponseData;
import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeRequestData;
import com.nivasafinance.features.lender.lenderoffice.dto.UpdateLenderOfficeRequest;
import com.nivasafinance.features.lender.lenderoffice.entity.LenderOffice;
import com.nivasafinance.features.lender.lenderoffice.enums.LenderOfficeStatus;
import com.nivasafinance.features.lender.lenderoffice.exception.LenderOfficeExceptionFactory;
import com.nivasafinance.features.lender.lenderoffice.repository.LenderOfficeRepositoryWrapper;
import com.nivasafinance.features.lender.lenderoffice.service.LenderOfficeWriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class LenderOfficeWriteServiceImpl implements LenderOfficeWriteService {

    private final AddressDataService addressDataService;
    private final LenderOfficeRepositoryWrapper lenderOfficeRepositoryWrapper;
    private final MessageSource messageSource;

    @Autowired
    public LenderOfficeWriteServiceImpl(
            AddressDataService addressDataService,
            LenderOfficeRepositoryWrapper lenderOfficeRepositoryWrapper,
            MessageSource messageSource) {
        this.addressDataService = addressDataService;
        this.lenderOfficeRepositoryWrapper = lenderOfficeRepositoryWrapper;
        this.messageSource = messageSource;
    }

    @Override
    public LenderOfficeReponseData create(String lenderKey, LenderOfficeRequestData lenderOfficeData) {
        String name = lenderOfficeData.getName();
        if (name == null || name.isBlank()) {
            throw LenderOfficeExceptionFactory.createFailed(messageSource);
        }
        Set<String> existingKeys = lenderOfficeRepositoryWrapper.findByLenderKey(lenderKey).stream()
                .map(LenderOffice::getKey)
                .collect(Collectors.toSet());
        String key = generateUniqueOfficeKey(normalizeKeyPart(name), existingKeys);

        com.nivasafinance.common.dto.AddressData addressData = null;
        if (lenderOfficeData.getCreateAddressRequest() != null) {
            addressData = addressDataService.createAddressData(lenderOfficeData.getCreateAddressRequest());
        }

        LenderOffice lenderOffice = new LenderOffice();
        lenderOffice.setName(name);
        lenderOffice.setKey(key);
        lenderOffice.setLenderKey(lenderKey);
        
        // Wrap AddressData in nested structure (like Lead does)
        if (addressData != null) {
            LenderOffice.AddressDetails addressDetails = LenderOffice.AddressDetails.builder()
                    .address(addressData)
                    .build();
            lenderOffice.setAddressDetails(addressDetails);
        }
        
        lenderOffice.setStatus(lenderOfficeData.getStatus() != null
                ? lenderOfficeData.getStatus()
                : LenderOfficeStatus.ACTIVE);

        LenderOffice savedLenderOffice = lenderOfficeRepositoryWrapper.saveWithException(lenderOffice);
        return toResponse(savedLenderOffice);
    }

    private static String normalizeKeyPart(String value) {
        return value.trim()
                .toUpperCase()
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
    }

    private static String generateUniqueOfficeKey(String normalizedBase, Set<String> existingKeys) {
        if (normalizedBase.isEmpty()) {
            normalizedBase = "OFFICE";
        }
        if (existingKeys == null || !existingKeys.contains(normalizedBase)) {
            return normalizedBase;
        }
        int counter = 1;
        String candidate;
        do {
            candidate = normalizedBase + "_" + counter;
            counter++;
        } while (existingKeys.contains(candidate));
        return candidate;
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
    public LenderOfficeReponseData updateOffice(UUID id, UpdateLenderOfficeRequest request) {
        if (request == null) {
            throw LenderOfficeExceptionFactory.createFailed(messageSource);
        }
        LenderOffice existing = lenderOfficeRepositoryWrapper.findByIdWithException(id);
        if (StringUtils.hasText(request.getName())) {
            existing.setName(request.getName().trim());
        }
        if (request.getCreateAddressRequest() != null) {
            com.nivasafinance.common.dto.AddressData addressData =
                    addressDataService.createAddressData(request.getCreateAddressRequest());
            existing.setAddressDetails(LenderOffice.AddressDetails.builder().address(addressData).build());
        }
        LenderOffice saved = lenderOfficeRepositoryWrapper.saveWithException(existing);
        return toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        lenderOfficeRepositoryWrapper.deleteByIdWithException(id);
    }

    @Override
    public LenderOfficeReponseData activateDeactivateLenderOffice(UUID officeId) {
        LenderOffice lenderOffice = lenderOfficeRepositoryWrapper.findByIdWithException(officeId);
        lenderOffice.setStatus(lenderOffice.getStatus() == LenderOfficeStatus.ACTIVE
                ? LenderOfficeStatus.INACTIVE
                : LenderOfficeStatus.ACTIVE);
        LenderOffice saved = lenderOfficeRepositoryWrapper.saveWithException(lenderOffice);
        return toResponse(saved);
    }

    private LenderOfficeReponseData toResponse(LenderOffice lenderOffice) {
        if (lenderOffice.getId() == null) {
            throw new IllegalStateException("Lender office ID cannot be null");
        }
        com.nivasafinance.common.dto.AddressData addressData = null;
        if (lenderOffice.getAddressDetails() != null) {
            addressData = lenderOffice.getAddressDetails().getAddress();
        }
        return new LenderOfficeReponseData(
                lenderOffice.getId(),
                lenderOffice.getName(),
                lenderOffice.getKey(),
                lenderOffice.getLenderKey(),
                addressData,
                lenderOffice.getStatus()
        );
    }
}

