package com.nivasafinance.features.lender.lenderoffice.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeReponseData;
import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeSearchRequest;
import com.nivasafinance.features.lender.lenderoffice.entity.LenderOffice;
import com.nivasafinance.features.lender.lenderoffice.enums.LenderOfficeStatus;
import com.nivasafinance.features.lender.lenderoffice.repository.LenderOfficeRepositoryWrapper;
import com.nivasafinance.features.lender.lenderoffice.service.LenderOfficeReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class LenderOfficeReadServiceImpl implements LenderOfficeReadService {

    private final LenderOfficeRepositoryWrapper lenderOfficeRepositoryWrapper;

    @Autowired
    public LenderOfficeReadServiceImpl(LenderOfficeRepositoryWrapper lenderOfficeRepositoryWrapper) {
        this.lenderOfficeRepositoryWrapper = lenderOfficeRepositoryWrapper;
    }

    @Override
    public LenderOfficeReponseData getByKey(String key) {
        LenderOffice lenderOffice = lenderOfficeRepositoryWrapper.findByKeyWithException(key);
        return toResponse(lenderOffice);
    }

    @Override
    public LenderOfficeReponseData getById(UUID id) {
        LenderOffice lenderOffice = lenderOfficeRepositoryWrapper.findByIdWithException(id);
        return toResponse(lenderOffice);
    }

    @Override
    public List<LenderOfficeReponseData> getByLenderKeyAndStatus(String lenderKey, LenderOfficeStatus status) {
        return lenderOfficeRepositoryWrapper.findByLenderKeyAndStatus(lenderKey, status).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatedResponse<LenderOfficeReponseData> getLenderOfficesPaginated(String lenderKey, PaginationRequest paginationRequest,
                                                                                LenderOfficeStatus status) {
        Page<LenderOffice> page = lenderOfficeRepositoryWrapper.findPage(lenderKey, paginationRequest, status);
        List<LenderOfficeReponseData> content = page.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        long total = page.getTotalElements();
        int limit = paginationRequest.getLimit();
        int offset = paginationRequest.getOffset();
        int totalPages = (int) Math.ceil((double) total / limit);
        int currentPage = limit > 0 ? offset / limit : 0;
        PaginationInfo paginationInfo = new PaginationInfo(
                offset,
                limit,
                total,
                totalPages,
                currentPage,
                (offset + limit) < total,
                offset > 0
        );
        return new PaginatedResponse<>(content, paginationInfo);
    }

    private static final int MIN_SEARCH_QUERY_LENGTH = 3;

    @Override
    public PaginatedResponse<LenderOfficeReponseData> searchLenderOffices(String lenderKey, PaginationRequest paginationRequest,
                                                                          LenderOfficeSearchRequest request) {
        if (!StringUtils.hasText(lenderKey)) {
            return emptyPaginatedResponse(paginationRequest);
        }
        if (request != null && StringUtils.hasText(request.getQuery()) && request.getQuery().trim().length() >= MIN_SEARCH_QUERY_LENGTH) {
            return lenderOfficeRepositoryWrapper.searchWithQuery(lenderKey, paginationRequest, request.getQuery(), request.getStatus());
        }
        return emptyPaginatedResponse(paginationRequest);
    }

    private static PaginatedResponse<LenderOfficeReponseData> emptyPaginatedResponse(PaginationRequest paginationRequest) {
        PaginationInfo info = PaginationInfo.builder()
                .offset(paginationRequest.getOffset())
                .limit(paginationRequest.getLimit())
                .totalElements(0)
                .totalPages(0)
                .currentPage(0)
                .hasNext(false)
                .hasPrevious(false)
                .build();
        return new PaginatedResponse<>(Collections.emptyList(), info);
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
                addressData,
                lenderOffice.getStatus()
        );
    }
}

