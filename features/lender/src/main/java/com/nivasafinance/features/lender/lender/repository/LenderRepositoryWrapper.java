package com.nivasafinance.features.lender.lender.repository;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lender.lender.dto.LenderResponseData;
import com.nivasafinance.features.lender.lender.entity.Lender;
import com.nivasafinance.features.lender.lender.enums.LenderStatus;
import com.nivasafinance.features.lender.lender.exception.LenderExceptionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class LenderRepositoryWrapper {

    private static final String TABLE = "n_lender";

    private final LenderRepository lenderRepository;
    private final MessageSource messageSource;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public LenderRepositoryWrapper(LenderRepository lenderRepository, MessageSource messageSource,
                                   JdbcTemplate jdbcTemplate) {
        this.lenderRepository = lenderRepository;
        this.messageSource = messageSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Lender saveWithException(Lender lender) {
        try {
            return lenderRepository.save(lender);
        } catch (Exception e) {
            throw LenderExceptionFactory.createFailed(messageSource);
        }
    }

    public Lender findByIdWithException(UUID id) {
        return lenderRepository.findById(id).orElseThrow(() ->
                LenderExceptionFactory.lenderNotFound(id, messageSource)
        );
    }

    public Lender findByKeyWithException(String key) {
        return lenderRepository.findByKey(key).orElseThrow(() ->
                LenderExceptionFactory.lenderNotFound(key, messageSource)
        );
    }

    public boolean existsByKey(String key) {
        return lenderRepository.findByKey(key).isPresent();
    }

    public List<Lender> findAll() {
        return lenderRepository.findAll();
    }

    public List<Lender> findAllByStatus(LenderStatus status) {
        return lenderRepository.findByStatus(status);
    }

    public Page<Lender> findPage(PaginationRequest paginationRequest, LenderStatus status) {
        Pageable pageable = toPageable(paginationRequest);
        if (status == null) {
            return lenderRepository.findAll(pageable);
        }
        return lenderRepository.findByStatus(status, pageable);
    }

    private static Pageable toPageable(PaginationRequest p) {
        int page = p.getLimit() > 0 ? p.getOffset() / p.getLimit() : 0;
        int size = Math.max(1, p.getLimit());
        Sort.Direction direction = "ASC".equalsIgnoreCase(p.getSortDirection()) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortBy = p.getSortBy() != null && !p.getSortBy().isBlank() ? p.getSortBy() : "createdAt";
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }

    public PaginatedResponse<LenderResponseData> searchWithQuery(PaginationRequest paginationRequest,
                                                                  String query, LenderStatus status) {
        if (!StringUtils.hasText(query)) {
            return new PaginatedResponse<>(Collections.emptyList(), buildPaginationInfo(paginationRequest, 0));
        }
        String pattern = "%" + query.trim() + "%";
        List<Object> params = new ArrayList<>();
        params.add(pattern);
        params.add(pattern);
        StringBuilder where = new StringBuilder(" WHERE (name ILIKE ? OR key ILIKE ?) ");
        if (status != null) {
            where.append(" AND status = ? ");
            params.add(status.name());
        }
        String countSql = "SELECT COUNT(id) FROM " + TABLE + where;
        List<Object> countParams = new ArrayList<>();
        countParams.add(pattern);
        countParams.add(pattern);
        if (status != null) {
            countParams.add(status.name());
        }
        params.add(paginationRequest.getLimit());
        params.add(paginationRequest.getOffset());
        String dataSql = "SELECT id, name, key, status FROM " + TABLE + where
                + " ORDER BY created_at DESC LIMIT ? OFFSET ? ";
        try {
            Long totalCount = jdbcTemplate.queryForObject(countSql, Long.class, countParams.toArray());
            long total = totalCount != null ? totalCount : 0L;
            List<LenderResponseData> results = jdbcTemplate.query(dataSql, new LenderSearchRowMapper(), params.toArray());
            return new PaginatedResponse<>(results, buildPaginationInfo(paginationRequest, total));
        } catch (EmptyResultDataAccessException e) {
            return new PaginatedResponse<>(Collections.emptyList(), buildPaginationInfo(paginationRequest, 0));
        } catch (DataAccessException e) {
            throw LenderExceptionFactory.retrieveFailed(messageSource);
        }
    }

    private static PaginationInfo buildPaginationInfo(PaginationRequest paginationRequest, long totalElements) {
        int limit = paginationRequest.getLimit();
        int offset = paginationRequest.getOffset();
        int totalPages = limit == 0 ? 0 : (int) Math.ceil((double) totalElements / limit);
        int currentPage = limit == 0 ? 0 : offset / limit;
        boolean hasNext = offset + limit < totalElements;
        boolean hasPrevious = offset > 0;
        return PaginationInfo.builder()
                .totalElements(totalElements)
                .totalPages(totalPages)
                .currentPage(currentPage)
                .limit(limit)
                .offset(offset)
                .hasNext(hasNext)
                .hasPrevious(hasPrevious)
                .build();
    }

    public void deleteByIdWithException(UUID id) {
        if (!lenderRepository.existsById(id)) {
            throw LenderExceptionFactory.lenderNotFound(id, messageSource);
        }
        lenderRepository.deleteById(id);
    }
}

