package com.nivasafinance.features.offices.service.impl;

import com.nivasafinance.features.offices.dto.OfficeResponse;
import com.nivasafinance.features.offices.entity.Office;
import com.nivasafinance.features.offices.exception.OfficeNotFoundException;
import com.nivasafinance.features.offices.repository.OfficeRepository;
import com.nivasafinance.features.offices.service.OfficeReadService;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class OfficeReadServiceImpl implements OfficeReadService {

    private final OfficeRepository officeRepository;
    private final MessageSource messageSource;

    @Override
    public OfficeResponse getOfficeByKey(String key) {
        Office entity = officeRepository.findByKey(key).orElseThrow(() ->
                new OfficeNotFoundException(key, messageSource)
        );
        return toResponse(entity);
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

