package com.nivasafinance.features.advisor.repository;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.advisor.dto.AdvisorSearchRequest;
import com.nivasafinance.features.advisor.dto.AdvisorSearchResponse;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.enums.AdvisorStatus;
import com.nivasafinance.features.advisor.exception.AdvisorExceptionFactory;
import com.nivasafinance.features.advisor.exception.AdvisorNotFoundException;
import com.nivasafinance.features.advisor.exception.AdvisorOperationException;
import com.nivasafinance.features.offices.service.OfficeReadService;
import com.nivasafinance.features.staff.service.StaffReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class AdvisorRepositoryWrapper {

    private final AdvisorRepository advisorRepository;
    private final MessageSource messageSource;
    private final JdbcTemplate jdbcTemplate;
    private final StaffReadService staffReadService;
    private final OfficeReadService officeReadService;

    @Autowired
    public AdvisorRepositoryWrapper(AdvisorRepository advisorRepository, MessageSource messageSource,
                                     JdbcTemplate jdbcTemplate, StaffReadService staffReadService,
                                     OfficeReadService officeReadService) {
        this.advisorRepository = advisorRepository;
        this.messageSource = messageSource;
        this.jdbcTemplate = jdbcTemplate;
        this.staffReadService = staffReadService;
        this.officeReadService = officeReadService;
    }

    public Advisor saveWithException(Advisor advisor) {
        try {
            return advisorRepository.save(advisor);
        } catch (DataAccessException e) {
            AdvisorOperationException exception = AdvisorExceptionFactory.createFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public Advisor findByIdWithException(UUID id) {
        try {
            return advisorRepository.findById(id).orElseThrow(() ->
                    AdvisorExceptionFactory.notFound(id, messageSource)
            );
        } catch (AdvisorNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            AdvisorOperationException exception = AdvisorExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public Advisor findByIdentifierWithException(UUID identifier) {
        try {
            return advisorRepository.findByIdentifier(identifier).orElseThrow(() ->
                    AdvisorExceptionFactory.notFound(identifier, messageSource)
            );
        } catch (AdvisorNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            AdvisorOperationException exception = AdvisorExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public Page<Advisor> findAllWithException(Pageable pageable) {
        try {
            return advisorRepository.findAll(pageable);
        } catch (DataAccessException e) {
            AdvisorOperationException exception = AdvisorExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public Long countWithException() {
        try {
            return advisorRepository.count();
        } catch (DataAccessException e) {
            AdvisorOperationException exception = AdvisorExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public void deleteByIdWithException(UUID id) {
        try {
            advisorRepository.deleteById(id);
        } catch (DataAccessException e) {
            AdvisorOperationException exception = AdvisorExceptionFactory.deleteFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    /**
     * Search advisors by phone number.
     * Finds advisors with persons that have the given phone number.
     * Applies office hierarchy filter to restrict to current staff's office hierarchy.
     *
     * @param paginationRequest Pagination parameters
     * @param request Search request containing mobile number
     * @return PaginatedResponse containing AdvisorSearchResponse objects
     */
    public PaginatedResponse<AdvisorSearchResponse> searchAdvisorsByPhoneNumber(
            PaginationRequest paginationRequest, AdvisorSearchRequest request) {
        // Validate phone number
        if (request == null || !StringUtils.hasText(request.getMobileNumber())) {
            return new PaginatedResponse<>(Collections.emptyList(),
                    buildPaginationInfo(paginationRequest, 0));
        }

        String mobileNumber = request.getMobileNumber().trim();
        
        // Get current staff office and code for hierarchy filtering
        String currentUserOfficeKey = staffReadService.getCurrentStaff().getOfficeKey();
        String currentUserOfficeCode = officeReadService.getOfficeByKey(currentUserOfficeKey).getCode();

        // Build SQL query to search advisors by phone number
        // Join advisor -> person -> mobile_numbers JSONB
        // Apply office hierarchy filter to restrict to current staff's office hierarchy
        String countSql = """
            SELECT COUNT(DISTINCT a.id)
            FROM n_advisor a
            LEFT JOIN n_office o ON o.key = a.office_key
            JOIN n_person p ON p.id = a.person_id
            WHERE EXISTS (
                SELECT 1 FROM jsonb_array_elements(COALESCE(p.mobile_numbers, '[]'::jsonb)) AS m
                WHERE m->>'number' = ?
            )
            AND o.code LIKE ?
            """;

        String dataSql = """
            SELECT DISTINCT
                a.identifier as advisor_identifier,
                p.display_name as person_name,
                (jsonb_path_query_first(COALESCE(p.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number') AS mobile_number,
                a.status,
                a.created_at,
                a.updated_at
            FROM n_advisor a
            LEFT JOIN n_office o ON o.key = a.office_key
            JOIN n_person p ON p.id = a.person_id
            WHERE EXISTS (
                SELECT 1 FROM jsonb_array_elements(COALESCE(p.mobile_numbers, '[]'::jsonb)) AS m
                WHERE m->>'number' = ?
            )
            AND o.code LIKE ?
            ORDER BY a.updated_at DESC
            LIMIT ? OFFSET ?
            """;

        try {
            String officePattern = currentUserOfficeCode + "%";
            
            // Get total count
            Long totalCount = jdbcTemplate.queryForObject(countSql, Long.class, mobileNumber, officePattern);
            long total = totalCount != null ? totalCount : 0L;

            // Get paginated data
            List<AdvisorSearchResponse> results = jdbcTemplate.query(
                    dataSql,
                    new AdvisorSearchRowMapper(),
                    mobileNumber,
                    officePattern,
                    paginationRequest.getLimit(),
                    paginationRequest.getOffset()
            );

            PaginationInfo paginationInfo = buildPaginationInfo(paginationRequest, total);
            return new PaginatedResponse<>(results, paginationInfo);
        } catch (EmptyResultDataAccessException e) {
            return new PaginatedResponse<>(Collections.emptyList(),
                    buildPaginationInfo(paginationRequest, 0));
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to search advisors by phone number", e);
        }
    }

    private PaginationInfo buildPaginationInfo(PaginationRequest paginationRequest, long totalElements) {
        int limit = paginationRequest.getLimit();
        int offset = paginationRequest.getOffset();
        int totalPages = limit == 0 ? 0 : (int) Math.ceil((double) totalElements / limit);
        int currentPage = limit == 0 ? 0 : offset / limit;
        boolean hasNext = offset + limit < totalElements;
        boolean hasPrevious = offset > 0;

        return PaginationInfo.builder()
                .offset(offset)
                .limit(limit)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .currentPage(currentPage)
                .hasNext(hasNext)
                .hasPrevious(hasPrevious)
                .build();
    }

    private static class AdvisorSearchRowMapper implements RowMapper<AdvisorSearchResponse> {
        @Override
        public AdvisorSearchResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            AdvisorSearchResponse.AdvisorSearchResponseBuilder builder = AdvisorSearchResponse.builder();

            String advisorIdentifierStr = rs.getString("advisor_identifier");
            if (advisorIdentifierStr != null) {
                builder.advisorIdentifier(UUID.fromString(advisorIdentifierStr));
            }

            builder.Name(rs.getString("person_name"));
            builder.mobileNumber(rs.getString("mobile_number"));

            String status = rs.getString("status");
            if (status != null) {
                try {
                    builder.status(AdvisorStatus.valueOf(status));
                } catch (IllegalArgumentException ignored) {
                    // Invalid status, leave as null
                }
            }

            java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
            if (createdAt != null) {
                builder.createdAt(createdAt.toLocalDateTime());
            }

            java.sql.Timestamp updatedAt = rs.getTimestamp("updated_at");
            if (updatedAt != null) {
                builder.updatedAt(updatedAt.toLocalDateTime());
            }

            return builder.build();
        }
    }
}

