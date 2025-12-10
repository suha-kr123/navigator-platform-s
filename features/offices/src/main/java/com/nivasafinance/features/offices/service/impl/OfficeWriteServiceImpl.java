package com.nivasafinance.features.offices.service.impl;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.features.address.service.AddressDataService;
import com.nivasafinance.features.offices.dto.OfficeCreateRequest;
import com.nivasafinance.features.offices.dto.OfficeResponse;
import com.nivasafinance.features.offices.entity.Office;
import com.nivasafinance.features.offices.repository.OfficeRepository;
import com.nivasafinance.features.offices.service.OfficeCodeFactory;
import com.nivasafinance.features.offices.service.OfficeWriteService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class OfficeWriteServiceImpl implements OfficeWriteService {

    private final OfficeRepository officeRepository;
    private final AddressDataService addressDataService;
    private final OfficeCodeFactory officeCodeFactory;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @org.springframework.cache.annotation.CacheEvict(cacheNames = {"offices", "officesByCodePrefix"}, allEntries = true)
    public OfficeResponse createOffice(OfficeCreateRequest request) {
        // Use common address data service for address creation
        Office office = new Office();
        if(request.getAddress() != null) {
            AddressData addressData = addressDataService.createAddressData(request.getAddress());
            office.setAddressData(addressData);
        }

        // Generate hierarchical code using factory
        String generatedCode = officeCodeFactory.generateOfficeCode(request.getParentId());

        // Create the office with address data embedded as JSONB
        office.setName(request.getName());
        office.setKey(request.getKey());
        office.setCode(generatedCode);
        office.setParentId(request.getParentId());

        Office savedOffice = officeRepository.save(office);
        return toResponse(savedOffice);
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

