package com.nivasafinance.features.offices.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.offices.dto.OfficeResponse;
import com.nivasafinance.features.offices.entity.Office;
import com.nivasafinance.features.offices.exception.OfficeNotFoundException;
import com.nivasafinance.features.offices.repository.OfficeRepository;
import com.nivasafinance.features.offices.repository.OfficeRepositoryWrapper;
import com.nivasafinance.features.offices.service.OfficeReadService;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class OfficeReadServiceImpl implements OfficeReadService {

    private final OfficeRepository officeRepository;
    private final OfficeRepositoryWrapper officeRepositoryWrapper;
    private final MessageSource messageSource;

    @Override
    public OfficeResponse getOfficeByKey(String key) {
        Office entity = findOfficeByKey(key);
        return toResponse(entity);
    }

    @Override
    public List<OfficeResponse> getOfficeByKeys(List<String> keys) {
       return keys.stream().map(this::findOfficeByKey).map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public PaginatedResponse<OfficeResponse> getOffices(String parentKey, String nameQuery, PaginationRequest paginationRequest) {
        String parentCodePrefix = null;
        if (StringUtils.hasText(parentKey)) {
            Office parentOffice = findOfficeByKey(parentKey);
            parentCodePrefix = parentOffice.getCode();
        }

        PaginatedResponse<Office> paginatedOffices = officeRepositoryWrapper.findOffices(
                parentCodePrefix,
                nameQuery,
                paginationRequest
        );

        return new PaginatedResponse<>(
                paginatedOffices.getContent().stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList()),
                paginatedOffices.getPagination()
        );
    }

    private Office findOfficeByKey(String key) {
        return officeRepository.findByKey(key).orElseThrow(() ->
                new OfficeNotFoundException(key, messageSource)
        );
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

