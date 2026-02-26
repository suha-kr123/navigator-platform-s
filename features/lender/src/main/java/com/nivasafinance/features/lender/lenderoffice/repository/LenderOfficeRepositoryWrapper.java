package com.nivasafinance.features.lender.lenderoffice.repository;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeReponseData;
import com.nivasafinance.features.lender.lenderoffice.entity.LenderOffice;
import com.nivasafinance.features.lender.lenderoffice.enums.LenderOfficeStatus;
import com.nivasafinance.features.lender.lenderoffice.exception.LenderOfficeExceptionFactory;
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
public class LenderOfficeRepositoryWrapper {

    private static final String TABLE = "n_lender_office";

    private final LenderOfficeRepository lenderOfficeRepository;
    private final MessageSource messageSource;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public LenderOfficeRepositoryWrapper(LenderOfficeRepository lenderOfficeRepository, MessageSource messageSource,
                                        JdbcTemplate jdbcTemplate) {
        this.lenderOfficeRepository = lenderOfficeRepository;
        this.messageSource = messageSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    public LenderOffice saveWithException(LenderOffice lenderOffice) {
        try {
            return lenderOfficeRepository.save(lenderOffice);
        } catch (Exception ex) {
            throw LenderOfficeExceptionFactory.createFailed(messageSource);
        }
    }

    public LenderOffice findByIdWithException(UUID id) {
        return lenderOfficeRepository.findById(id).orElseThrow(() ->
                LenderOfficeExceptionFactory.lenderOfficeNotFound(id, messageSource)
        );
    }

    public LenderOffice findByKeyWithException(String key) {
        return lenderOfficeRepository.findByKey(key).orElseThrow(() ->
                LenderOfficeExceptionFactory.lenderOfficeKeyNotFound(key, messageSource)
        );
    }

    public List<LenderOffice> findByLenderKey(String lenderKey) {
        return lenderOfficeRepository.findByLenderKey(lenderKey);
    }

    public List<LenderOffice> findByLenderKeyAndStatus(String lenderKey, LenderOfficeStatus status) {
        return lenderOfficeRepository.findByLenderKeyAndStatus(lenderKey, status);
    }

    public Page<LenderOffice> findPage(String lenderKey, PaginationRequest paginationRequest, LenderOfficeStatus status) {
        Pageable pageable = toPageable(paginationRequest);
        if (status == null) {
            return lenderOfficeRepository.findByLenderKey(lenderKey, pageable);
        }
        return lenderOfficeRepository.findByLenderKeyAndStatus(lenderKey, status, pageable);
    }

    private static Pageable toPageable(PaginationRequest p) {
        int page = p.getLimit() > 0 ? p.getOffset() / p.getLimit() : 0;
        int size = Math.max(1, p.getLimit());
        Sort.Direction direction = "ASC".equalsIgnoreCase(p.getSortDirection()) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortBy = p.getSortBy() != null && !p.getSortBy().isBlank() ? p.getSortBy() : "createdAt";
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }

    public void deleteByIdWithException(UUID id) {
        if (!lenderOfficeRepository.existsById(id)) {
            throw LenderOfficeExceptionFactory.lenderOfficeNotFound(id, messageSource);
        }
        lenderOfficeRepository.deleteById(id);
    }

    public PaginatedResponse<LenderOfficeReponseData> searchWithQuery(String lenderKey, PaginationRequest paginationRequest,
                                                                      String query, LenderOfficeStatus status) {
        if (!StringUtils.hasText(query) || !StringUtils.hasText(lenderKey)) {
            return new PaginatedResponse<>(Collections.emptyList(), buildPaginationInfo(paginationRequest, 0));
        }
        String pattern = "%" + query.trim() + "%";
        List<Object> params = new ArrayList<>();
        params.add(lenderKey);
        params.add(pattern);
        params.add(pattern);
        StringBuilder where = new StringBuilder(" WHERE lender_key = ? AND (name ILIKE ? OR key ILIKE ?) ");
        if (status != null) {
            where.append(" AND status = ? ");
            params.add(status.name());
        }
        List<Object> countParams = new ArrayList<>(params);
        params.add(paginationRequest.getLimit());
        params.add(paginationRequest.getOffset());
        String countSql = "SELECT COUNT(id) FROM " + TABLE + where;
        String dataSql = "SELECT id, name, key, lender_key, address, status FROM " + TABLE + where
                + " ORDER BY created_at DESC LIMIT ? OFFSET ? ";
        try {
            Long totalCount = jdbcTemplate.queryForObject(countSql, Long.class, countParams.toArray());
            long total = totalCount != null ? totalCount : 0L;
            List<LenderOfficeReponseData> results = jdbcTemplate.query(dataSql, new LenderOfficeSearchRowMapper(), params.toArray());
            return new PaginatedResponse<>(results, buildPaginationInfo(paginationRequest, total));
        } catch (EmptyResultDataAccessException e) {
            return new PaginatedResponse<>(Collections.emptyList(), buildPaginationInfo(paginationRequest, 0));
        } catch (DataAccessException e) {
            throw LenderOfficeExceptionFactory.createFailed(messageSource);
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
}

