package com.nivasafinance.features.master.codemaster.repository;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeSearchResponse;
import com.nivasafinance.features.master.codemaster.entity.MasterCode;
import com.nivasafinance.features.master.codemaster.exception.CodeMasterExceptionFactory;
import com.nivasafinance.features.master.codemaster.exception.CodeMasterNotFoundException;
import com.nivasafinance.features.master.codemaster.exception.CodeMasterOperationException;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MasterCodeRepositoryWrapper {

    private static final String SEARCH_BASE = """
            SELECT mc.key,
                   mc.key AS code_key,
                   mc.name->>'default' AS display_text,
                   mc.description->>'default' AS description,
                   mc.parent_id
            FROM n_master_code mc
            WHERE mc.name IS NOT NULL
              AND mc.name->>'default' IS NOT NULL
              AND mc.name->>'default' ILIKE :search
            """;

    private static final String PARENT_ID_NULL_FILTER = " AND mc.parent_id IS NULL";
    private static final String PARENT_ID_NOT_NULL_FILTER = " AND mc.parent_id IS NOT NULL";

    private final MasterCodeRepository masterCodeRepository;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final MessageSource messageSource;
    private final CodeMasterExceptionFactory codeMasterExceptionFactory;

    public MasterCodeRepositoryWrapper(MasterCodeRepository masterCodeRepository,
                                       NamedParameterJdbcTemplate jdbcTemplate,
                                       MessageSource messageSource) {
        this.masterCodeRepository = masterCodeRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.messageSource = messageSource;
        this.codeMasterExceptionFactory = new CodeMasterExceptionFactory(messageSource);
    }

    public MasterCode saveWithException(MasterCode masterCode) {
        try {
            return masterCodeRepository.save(masterCode);
        } catch (DataAccessException e) {
            CodeMasterOperationException exception = codeMasterExceptionFactory.createFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public MasterCode findByIdWithException(Long id) {
        try {
            return masterCodeRepository.findById(id).orElseThrow(() ->
                    codeMasterExceptionFactory.notFoundById(id, messageSource));
        } catch (CodeMasterNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            CodeMasterOperationException exception = codeMasterExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public MasterCode findByKeyWithException(String key) {
        try {
            return masterCodeRepository.findByKey(key).orElseThrow(() ->
                    codeMasterExceptionFactory.notFound(key, messageSource));
        } catch (CodeMasterNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            CodeMasterOperationException exception = codeMasterExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public List<MasterCode> findByParentIdWithException(Long parentId) {
        try {
            return masterCodeRepository.findByParentId(parentId);
        } catch (DataAccessException e) {
            CodeMasterOperationException exception = codeMasterExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public PaginatedResponse<MasterCode> findByParentIdWithException(Long parentId, PaginationRequest request) {
        try {
            Pageable pageable = buildPageable(request);
            Page<MasterCode> page = masterCodeRepository.findByParentId(parentId, pageable);
            return buildPaginatedResponse(page, request);
        } catch (DataAccessException e) {
            throw buildRetrieveException(e);
        }
    }

    public List<MasterCode> findAllWithException() {
        try {
            return masterCodeRepository.findAll();
        } catch (DataAccessException e) {
            CodeMasterOperationException exception = codeMasterExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public void deleteByIdWithException(Long id) {
        if (!masterCodeRepository.existsById(id)) {
            throw codeMasterExceptionFactory.notFoundById(id, messageSource);
        }
        try {
            masterCodeRepository.deleteById(id);
        } catch (DataAccessException e) {
            CodeMasterOperationException exception = codeMasterExceptionFactory.deleteFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public PaginatedResponse<MasterCode> findAllWithException(PaginationRequest request) {
        try {
            Pageable pageable = buildPageable(request);
            Page<MasterCode> page = masterCodeRepository.findAll(pageable);
            return buildPaginatedResponse(page, request);
        } catch (DataAccessException e) {
            throw buildRetrieveException(e);
        }
    }

    private Pageable buildPageable(PaginationRequest request) {
        int offset = request.getOffset();
        int limit = request.getLimit();

        String sortBy = request.getSortBy();
        String direction = request.getSortDirection();

        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        return PageRequest.of(offset / limit, limit, sort);
    }

    private PaginatedResponse<MasterCode> buildPaginatedResponse(
            Page<MasterCode> page,
            PaginationRequest request
    ) {
        PaginationInfo pageInfo = new PaginationInfo(
                request.getOffset(),
                request.getLimit(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.hasNext(),
                page.hasPrevious()
        );

        return new PaginatedResponse<>(page.getContent(), pageInfo);
    }

    private CodeMasterOperationException buildRetrieveException(Exception e) {
        CodeMasterOperationException ex =
                codeMasterExceptionFactory.retrieveEntityFailed(messageSource);
        ex.initCause(e);
        return ex;
    }

    public PaginatedResponse<MasterCodeSearchResponse> searchMasterCodesOnly(
            String searchTerm, PaginationRequest request) {
        return searchMasterCodeNamesByParentFilter(searchTerm, request, PARENT_ID_NULL_FILTER);
    }

    public PaginatedResponse<MasterCodeSearchResponse> searchChildCodesOnly(
            String searchTerm, PaginationRequest request) {
        return searchMasterCodeNamesByParentFilter(searchTerm, request, PARENT_ID_NOT_NULL_FILTER);
    }

    private PaginatedResponse<MasterCodeSearchResponse> searchMasterCodeNamesByParentFilter(
            String searchTerm, PaginationRequest request, String parentFilter) {
        try {
            String searchPattern = "%" + searchTerm.trim() + "%";
            MapSqlParameterSource params = new MapSqlParameterSource("search", searchPattern);

            StringBuilder countQuery = new StringBuilder("SELECT COUNT(*) FROM n_master_code mc WHERE mc.name IS NOT NULL");
            countQuery.append(" AND mc.name->>'default' IS NOT NULL");
            countQuery.append(" AND mc.name->>'default' ILIKE :search");
            countQuery.append(parentFilter);
            long totalElements = jdbcTemplate.queryForObject(countQuery.toString(), params, Long.class);

            StringBuilder selectQuery = new StringBuilder(SEARCH_BASE);
            selectQuery.append(parentFilter);
            selectQuery.append(" ORDER BY mc.key ASC LIMIT :limit OFFSET :offset");
            params.addValue("limit", request.getLimit());
            params.addValue("offset", request.getOffset());

            List<MasterCodeSearchResponse> content = jdbcTemplate.query(
                    selectQuery.toString(), params, new MasterCodeSearchRowMapper());

            int totalPages = request.getLimit() == 0 ? 0 : (int) Math.ceil(totalElements / (double) request.getLimit());
            int currentPage = request.getLimit() == 0 ? 0 : request.getOffset() / request.getLimit();
            PaginationInfo pageInfo = new PaginationInfo(
                    request.getOffset(),
                    request.getLimit(),
                    totalElements,
                    totalPages,
                    currentPage,
                    request.getOffset() + request.getLimit() < totalElements,
                    request.getOffset() > 0);

            return new PaginatedResponse<>(content, pageInfo);
        } catch (DataAccessException e) {
            throw buildRetrieveException(e);
        }
    }

}

