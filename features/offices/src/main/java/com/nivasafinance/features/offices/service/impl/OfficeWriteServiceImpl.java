package com.nivasafinance.features.offices.service.impl;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.features.address.service.AddressDataService;
import com.nivasafinance.features.offices.dto.OfficeCreateRequest;
import com.nivasafinance.features.offices.dto.OfficeResponse;
import com.nivasafinance.features.offices.entity.Office;
import com.nivasafinance.features.offices.exception.OfficeExceptionFactory;
import com.nivasafinance.features.offices.exception.OfficeNotFoundException;
import com.nivasafinance.features.offices.repository.OfficeRepository;
import com.nivasafinance.features.offices.repository.OfficeRepositoryWrapper;
import com.nivasafinance.features.offices.service.OfficeCodeFactory;
import com.nivasafinance.features.offices.service.OfficeWriteService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class OfficeWriteServiceImpl implements OfficeWriteService {

    private final OfficeRepository officeRepository;
    private final OfficeRepositoryWrapper officeRepositoryWrapper;
    private final AddressDataService addressDataService;
    private final OfficeCodeFactory officeCodeFactory;
    private final MessageSource messageSource;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @org.springframework.cache.annotation.CacheEvict(cacheNames = {"offices", "officesByCodePrefix"}, allEntries = true)
    public OfficeResponse createOffice(OfficeCreateRequest request) {
        if (officeRepository.findByKey(request.getKey()).isPresent()) {
            throw OfficeExceptionFactory.duplicateKey(request.getKey());
        }
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
        office.setIsActive(Boolean.TRUE);

        try {
            Office savedOffice = officeRepository.save(office);
            return toResponse(savedOffice);
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage() != null && e.getMessage().contains("n_office_key_key")) {
                throw OfficeExceptionFactory.duplicateKey(request.getKey());
            }
            throw e;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @org.springframework.cache.annotation.CacheEvict(cacheNames = {"offices", "officesByCodePrefix"}, allEntries = true)
    public OfficeResponse moveOffice(String officeKey, String newParentKey) {
        Office office = officeRepository.findByKey(officeKey)
                .orElseThrow(() -> new OfficeNotFoundException(officeKey, messageSource));
        Long newParentId = null;
        if (newParentKey != null && !newParentKey.isBlank()) {
            Office newParent = officeRepository.findByKey(newParentKey)
                    .orElseThrow(() -> new OfficeNotFoundException(newParentKey, messageSource));
            if (newParent.getCode() != null && newParent.getCode().startsWith(office.getCode() + ".")) {
                throw new IllegalArgumentException("Cannot move office under its own descendant");
            }
            newParentId = newParent.getId();
        }
        String oldPrefix = office.getCode();
        String newPrefix = officeCodeFactory.generateOfficeCode(newParentId);
        office.setParentId(newParentId);
        office.setCode(newPrefix);
        officeRepository.save(office);
        List<Office> descendants = officeRepositoryWrapper.findAllByCodePrefix(oldPrefix);
        for (Office child : descendants) {
            if (child.getId().equals(office.getId())) {
                continue;
            }
            String updatedCode = child.getCode().replaceFirst(Pattern.quote(oldPrefix), newPrefix);
            child.setCode(updatedCode);
            officeRepository.save(child);
        }
        return toResponse(office);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @org.springframework.cache.annotation.CacheEvict(cacheNames = {"offices", "officesByCodePrefix"}, allEntries = true)
    public OfficeResponse activateOffice(String officeKey) {
        Office office = officeRepository.findByKey(officeKey)
                .orElseThrow(() -> new OfficeNotFoundException(officeKey, messageSource));
        office.setIsActive(Boolean.TRUE);
        officeRepository.save(office);
        return toResponse(office);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @org.springframework.cache.annotation.CacheEvict(cacheNames = {"offices", "officesByCodePrefix"}, allEntries = true)
    public OfficeResponse deactivateOfficeCascade(String officeKey) {
        Office office = officeRepository.findByKey(officeKey)
                .orElseThrow(() -> new OfficeNotFoundException(officeKey, messageSource));
        String prefix = office.getCode();
        List<Office> affected = officeRepositoryWrapper.findAllByCodePrefix(prefix);
        for (Office o : affected) {
            o.setIsActive(Boolean.FALSE);
            officeRepository.save(o);
        }
        return toResponse(officeRepository.findByKey(officeKey)
                .orElseThrow(() -> new OfficeNotFoundException(officeKey, messageSource)));
    }

    private OfficeResponse toResponse(Office entity) {
        return new OfficeResponse(
                entity.getId(),
                entity.getName(),
                entity.getKey(),
                entity.getCode(),
                entity.getAddressData(),
                entity.getParentId(),
                entity.getIsActive()
        );
    }
}
