package com.nivasafinance.features.bre.repository;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.bre.entity.BREConfigs;
import com.nivasafinance.features.bre.exception.BREConfigExceptionFactory;
import com.nivasafinance.features.bre.exception.BREConfigNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BREConfigRepositoryWrapper {

    private final BREConfigRepository breConfigRepository;
    private final MessageSource messageSource;

    public BREConfigs saveWithException(BREConfigs entity) {
        try {
            return breConfigRepository.save(entity);
        } catch (DataAccessException e) {
            throw BREConfigExceptionFactory.saveFailed(messageSource);
        }
    }

    public BREConfigs findByUnameWithException(String uname) {
        try {
            return breConfigRepository.findByUname(uname).orElseThrow(() ->
                    BREConfigExceptionFactory.notFoundByUname(uname, messageSource));
        } catch (BREConfigNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            throw BREConfigExceptionFactory.retrieveByUnameFailed(uname, messageSource);
        }
    }

    public PaginatedResponse<BREConfigs> findAllWithException(PaginationRequest paginationRequest) {
        try {
            int offset = paginationRequest.getOffset();
            int limit = paginationRequest.getLimit();
            String sortBy = paginationRequest.getSortBy() != null ? paginationRequest.getSortBy() : "createdAt";
            String sortDirection = paginationRequest.getSortDirection() != null ? paginationRequest.getSortDirection() : "DESC";

            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            Pageable pageable = PageRequest.of(offset / limit, limit, sort);

            Page<BREConfigs> page = breConfigRepository.findAll(pageable);
            PaginationInfo paginationInfo = new PaginationInfo(
                    offset, limit, page.getTotalElements(), page.getTotalPages(),
                    page.getNumber(), page.hasNext(), page.hasPrevious());

            return new PaginatedResponse<>(page.getContent(), paginationInfo);
        } catch (DataAccessException e) {
            throw BREConfigExceptionFactory.retrieveAllFailed(messageSource);
        }
    }
}
