package com.nivasafinance.features.advisorlead.repository;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.advisorlead.entity.AdvisorLeadMapping;
import com.nivasafinance.features.advisorlead.exception.AdvisorLeadMappingExceptionFactory;
import com.nivasafinance.features.advisorlead.exception.AdvisorLeadMappingOperationException;
import com.nivasafinance.features.lead.dto.LeadBasicResponse;
import com.nivasafinance.features.lead.enums.LeadStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class AdvisorLeadMappingRepositoryWrapper {

    private final AdvisorLeadMappingRepository advisorLeadMappingRepository;
    private final MessageSource messageSource;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public AdvisorLeadMappingRepositoryWrapper(
            AdvisorLeadMappingRepository advisorLeadMappingRepository,
            MessageSource messageSource,
            JdbcTemplate jdbcTemplate) {
        this.advisorLeadMappingRepository = advisorLeadMappingRepository;
        this.messageSource = messageSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    public AdvisorLeadMapping saveWithException(AdvisorLeadMapping advisorLeadMapping) {
        try {
            return advisorLeadMappingRepository.save(advisorLeadMapping);
        } catch (DataAccessException e) {
            AdvisorLeadMappingOperationException exception = AdvisorLeadMappingExceptionFactory.createFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public AdvisorLeadMapping findByIdWithException(UUID id) {
        try {
            return advisorLeadMappingRepository.findById(id).orElseThrow(() ->
                    AdvisorLeadMappingExceptionFactory.notFound(id, messageSource)
            );
        } catch (DataAccessException e) {
            AdvisorLeadMappingOperationException exception = AdvisorLeadMappingExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public Page<AdvisorLeadMapping> findAllByAdvisorIdWithException(UUID advisorId, Pageable pageable) {
        try {
            return advisorLeadMappingRepository.findAllByAdvisorId(advisorId, pageable);
        } catch (DataAccessException e) {
            AdvisorLeadMappingOperationException exception = AdvisorLeadMappingExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    public void deleteByIdWithException(UUID id) {
        try {
            advisorLeadMappingRepository.deleteById(id);
        } catch (DataAccessException e) {
            AdvisorLeadMappingOperationException exception = AdvisorLeadMappingExceptionFactory.deleteFailed(messageSource);
            exception.initCause(e);
            throw exception;
        }
    }

    /**
     * Find leads by advisor identifier with pagination.
     * Returns a paginated list of LeadBasicResponse for the given advisor.
     *
     * @param advisorIdentifier The advisor UUID identifier
     * @param paginationRequest Pagination parameters
     * @return PaginatedResponse containing LeadBasicResponse objects
     */
    public PaginatedResponse<LeadBasicResponse> findLeadsByAdvisorIdWithException(
            UUID advisorIdentifier, PaginationRequest paginationRequest) {
        String countSql = """
            SELECT COUNT(DISTINCT l.id)
            FROM n_advisor a
            JOIN n_advisor_lead_mapping alm ON alm.advisor_id = a.id
            JOIN n_lead l ON l.id = alm.lead_id
            WHERE a.identifier = ?
            """;

        String dataSql = """
            SELECT DISTINCT
                l.lead_identifier as leadIdentifier,
                l.requested_amount as requestedAmount,
                l.status,
                l.created_at as createdAt,
                l.workflow_details->>'currentStage' as currentStage,
                primary_person.display_name as primaryContactName,
                (jsonb_path_query_first(COALESCE(primary_person.mobile_numbers, '[]'::jsonb), '$[*] ? (@.isPrimary == true)') ->> 'number') AS primaryContactPhone,
                o.name as office
            FROM n_advisor a
            JOIN n_advisor_lead_mapping alm ON alm.advisor_id = a.id
            JOIN n_lead l ON l.id = alm.lead_id
            LEFT JOIN n_contact primary_contact ON primary_contact.id = (l.other_details->>'primaryContactId')::bigint
            LEFT JOIN n_person primary_person ON primary_contact.person_id = primary_person.id
            LEFT JOIN n_office o ON o.key = l.office_key
            WHERE a.identifier = ?
            ORDER BY l.created_at DESC
            LIMIT ? OFFSET ?
            """;

        try {
            // Get total count
            Long totalCount = jdbcTemplate.queryForObject(countSql, Long.class, advisorIdentifier);
            long total = totalCount != null ? totalCount : 0L;

            // Get paginated data
            List<LeadBasicResponse> results = jdbcTemplate.query(
                    dataSql,
                    new LeadBasicResponseRowMapper(),
                    advisorIdentifier,
                    paginationRequest.getLimit(),
                    paginationRequest.getOffset()
            );

            PaginationInfo paginationInfo = buildPaginationInfo(paginationRequest, total);
            return new PaginatedResponse<>(results, paginationInfo);
        } catch (EmptyResultDataAccessException e) {
            return new PaginatedResponse<>(Collections.emptyList(),
                    buildPaginationInfo(paginationRequest, 0));
        } catch (DataAccessException e) {
            AdvisorLeadMappingOperationException exception = AdvisorLeadMappingExceptionFactory.retrieveEntityFailed(messageSource);
            exception.initCause(e);
            throw exception;
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

    private static class LeadBasicResponseRowMapper implements RowMapper<LeadBasicResponse> {
        @Override
        public LeadBasicResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            LeadBasicResponse.LeadBasicResponseBuilder builder = LeadBasicResponse.builder();

            String leadIdentifierStr = rs.getString("leadIdentifier");
            if (leadIdentifierStr != null) {
                builder.leadIdentifier(UUID.fromString(leadIdentifierStr));
            }

            builder.requestedAmount(rs.getBigDecimal("requestedAmount"));
            builder.primaryContactName(rs.getString("primaryContactName"));
            builder.primaryContactPhone(rs.getString("primaryContactPhone"));
            builder.currentStage(rs.getString("currentStage"));
            builder.office(rs.getString("office"));

            String status = rs.getString("status");
            if (status != null) {
                try {
                    builder.status(LeadStatus.valueOf(status));
                } catch (IllegalArgumentException ignored) {
                    // Invalid status, leave as null
                }
            }

            java.sql.Timestamp createdAt = rs.getTimestamp("createdAt");
            if (createdAt != null) {
                builder.createdAt(createdAt.toLocalDateTime());
            }

            return builder.build();
        }
    }
}

