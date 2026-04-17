package com.nivasafinance.features.master.codemaster.repository;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeSearchResponse;
import com.nivasafinance.features.master.codemaster.entity.MasterCodeValue;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MasterCodeValueRepositoryWrapper {

    private static final String SEARCH_SELECT = """
            SELECT mcv.key,
                   mcv.code_key,
                   mcv.value->>'default' AS display_text,
                   mcv.description->>'default' AS description,
                   mcv.is_active
            FROM n_master_code_value mcv
            WHERE mcv.value IS NOT NULL
              AND mcv.value->>'default' IS NOT NULL
              AND mcv.value->>'default' ILIKE :search
            """;

    private static final String SEARCH_COUNT = """
            SELECT COUNT(*)
            FROM n_master_code_value mcv
            WHERE mcv.value IS NOT NULL
              AND mcv.value->>'default' IS NOT NULL
              AND mcv.value->>'default' ILIKE :search
            """;

    private final MasterCodeValueRepository masterCodeValueRepository;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final MessageSource messageSource;
    private final CodeMasterExceptionFactory codeMasterExceptionFactory;

    public MasterCodeValueRepositoryWrapper(MasterCodeValueRepository masterCodeValueRepository,
                                            NamedParameterJdbcTemplate jdbcTemplate,
                                            MessageSource messageSource) {
        this.masterCodeValueRepository = masterCodeValueRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.messageSource = messageSource;
        this.codeMasterExceptionFactory = new CodeMasterExceptionFactory(messageSource);
    }

    public MasterCodeValue saveWithException(MasterCodeValue masterCodeValue) {
        try {
            return masterCodeValueRepository.save(masterCodeValue);
        } catch (DataAccessException e) {
            CodeMasterOperationException exception = codeMasterExceptionFactory.createFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public MasterCodeValue findByIdWithException(Long id) {
        try {
            return masterCodeValueRepository.findById(id).orElseThrow(() ->
                    codeMasterExceptionFactory.notFoundById(id, messageSource));
        } catch (CodeMasterNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            CodeMasterOperationException exception = codeMasterExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public MasterCodeValue findByKeyAndCodeKeyWithException(String key, String codeKey) {
        try {
            return masterCodeValueRepository.findByKeyAndCodeKey(key, codeKey).orElseThrow(() ->
                    codeMasterExceptionFactory.notFound("Key: " + key + ", CodeKey: " + codeKey, messageSource));
        } catch (CodeMasterNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            CodeMasterOperationException exception = codeMasterExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public List<MasterCodeValue> findByCodeKeyWithException(String codeKey) {
        try {
            return masterCodeValueRepository.findByCodeKeyOrderByDisplayOrderAsc(codeKey);
        } catch (DataAccessException e) {
            CodeMasterOperationException exception = codeMasterExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public List<MasterCodeValue> findByCodeKeyAndIsActiveTrueWithException(String codeKey) {
        try {
            return masterCodeValueRepository.findByCodeKeyAndIsActiveTrueOrderByDisplayOrderAsc(codeKey);
        } catch (DataAccessException e) {
            CodeMasterOperationException exception = codeMasterExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public PaginatedResponse<MasterCodeValue> findByCodeKeyWithException(
            String codeKey, Boolean onlyActive, PaginationRequest request) {
        try {
            Pageable pageable = buildPageable(request);
            Page<MasterCodeValue> page = Boolean.TRUE.equals(onlyActive)
                    ? masterCodeValueRepository.findByCodeKeyAndIsActiveTrue(codeKey, pageable)
                    : masterCodeValueRepository.findByCodeKey(codeKey, pageable);
            return buildPaginatedResponse(page, request);
        } catch (DataAccessException e) {
            CodeMasterOperationException ex = codeMasterExceptionFactory.retrieveEntityFailed(messageSource);
            ex.initCause(e);
            throw ex;
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

    private PaginatedResponse<MasterCodeValue> buildPaginatedResponse(
            Page<MasterCodeValue> page, PaginationRequest request) {
        PaginationInfo pageInfo = new PaginationInfo(
                request.getOffset(),
                request.getLimit(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.hasNext(),
                page.hasPrevious());
        return new PaginatedResponse<>(page.getContent(), pageInfo);
    }

    public List<MasterCodeValue> findAllWithException() {
        try {
            return masterCodeValueRepository.findAll();
        } catch (DataAccessException e) {
            CodeMasterOperationException exception = codeMasterExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public void deleteByIdWithException(Long id) {
        if (!masterCodeValueRepository.existsById(id)) {
            throw codeMasterExceptionFactory.notFoundById(id, messageSource);
        }
        try {
            masterCodeValueRepository.deleteById(id);
        } catch (DataAccessException e) {
            CodeMasterOperationException exception = codeMasterExceptionFactory.deleteFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }
    public Optional<MasterCodeValue> findByKey(String key) {
        return masterCodeValueRepository.findByKey(key);
    }

    public MasterCodeValue findByKeyWithException(String key) {
        return masterCodeValueRepository.findByKey(key).orElseThrow(() ->
                codeMasterExceptionFactory.codeValueKeyNotFound(key, messageSource));
    }

    public Set<String> findAllKeysWithException() {
        try {
            return masterCodeValueRepository.findAll().stream()
                    .map(MasterCodeValue::getKey)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        } catch (DataAccessException e) {
            throw codeMasterExceptionFactory.retrieveEntityFailed(messageSource);
        }
    }

    public List<MasterCodeValue> saveAllWithException(List<MasterCodeValue> masterCodeValues) {
        try {
            String masterCodeKey = masterCodeValues.get(0).getCodeKey();
            masterCodeValueRepository.saveAll(masterCodeValues);
            return findByCodeKeyWithException(masterCodeKey);
        } catch (DataAccessException e) {
            throw codeMasterExceptionFactory.retrieveEntityFailed(messageSource);
        }
    }

    public PaginatedResponse<MasterCodeSearchResponse> searchMasterCodeValues(
            String searchTerm, String codeKey, PaginationRequest request) {
        try {
            String searchPattern = "%" + searchTerm.trim() + "%";
            MapSqlParameterSource params = new MapSqlParameterSource("search", searchPattern);
            params.addValue("codeKey", codeKey);

            String codeKeyFilter = " AND mcv.code_key = :codeKey";

            StringBuilder countQuery = new StringBuilder(SEARCH_COUNT);
            countQuery.append(codeKeyFilter);
            Long totalElements = jdbcTemplate.queryForObject(countQuery.toString(), params, Long.class);
            if (totalElements == null) {
                totalElements = 0L;
            }

            StringBuilder selectQuery = new StringBuilder(SEARCH_SELECT);
            selectQuery.append(codeKeyFilter);
            selectQuery.append(" ORDER BY mcv.key ASC LIMIT :limit OFFSET :offset");
            params.addValue("limit", request.getLimit());
            params.addValue("offset", request.getOffset());

            List<MasterCodeSearchResponse> content = jdbcTemplate.query(
                    selectQuery.toString(), params, new MasterCodeValueSearchRowMapper());

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
            CodeMasterOperationException ex = codeMasterExceptionFactory.retrieveEntityFailed(messageSource);
            ex.initCause(e);
            throw ex;
        }
    }

}   

