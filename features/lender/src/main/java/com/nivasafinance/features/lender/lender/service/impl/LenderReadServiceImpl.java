package com.nivasafinance.features.lender.lender.service.impl;

import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lender.lender.dto.LenderResponseData;
import com.nivasafinance.features.lender.lender.dto.LenderSearchRequest;
import com.nivasafinance.features.lender.lender.entity.Lender;
import com.nivasafinance.features.lender.lender.enums.LenderStatus;
import com.nivasafinance.features.lender.lender.repository.LenderRepositoryWrapper;
import com.nivasafinance.features.lender.lender.service.LenderReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class LenderReadServiceImpl implements LenderReadService {

    private final LenderRepositoryWrapper lenderRepositoryWrapper;

    @Autowired
    public LenderReadServiceImpl(LenderRepositoryWrapper lenderRepositoryWrapper) {
        this.lenderRepositoryWrapper = lenderRepositoryWrapper;
    }

    @Override
    public LenderResponseData getById(UUID id) {
        Lender lender = lenderRepositoryWrapper.findByIdWithException(id);
        return toResponse(lender);
    }

    @Override
    public LenderResponseData getByKey(String key) {
        Lender lender = lenderRepositoryWrapper.findByKeyWithException(key);
        return toResponse(lender);
    }

    @Override
    public List<LenderResponseData> getAllByStatus(LenderStatus status) {
        return lenderRepositoryWrapper.findAllByStatus(status).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatedResponse<LenderResponseData> getLendersPaginated(PaginationRequest paginationRequest, LenderStatus status) {
        Page<Lender> page = lenderRepositoryWrapper.findPage(paginationRequest, status);
        List<LenderResponseData> content = page.getContent().stream()
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
    public PaginatedResponse<LenderResponseData> searchLenders(PaginationRequest paginationRequest, LenderSearchRequest request) {
        if (request != null && StringUtils.hasText(request.getQuery()) && request.getQuery().trim().length() >= MIN_SEARCH_QUERY_LENGTH) {
            return lenderRepositoryWrapper.searchWithQuery(paginationRequest, request.getQuery(), request.getStatus());
        }
        return getLendersPaginated(paginationRequest, request != null ? request.getStatus() : null);
    }

    private LenderResponseData toResponse(Lender lender) {
        if (lender.getId() == null) {
            throw new IllegalStateException("Lender ID cannot be null");
        }
        return new LenderResponseData(
                lender.getId(),
                lender.getName(),
                lender.getKey(),
                lender.getStatus()
        );
    }
}

