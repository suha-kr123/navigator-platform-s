package com.nivasafinance.features.lead.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lead.dto.LeadDocumentResponse;
import com.nivasafinance.features.lead.enums.LeadDocumentStatus;
import com.nivasafinance.features.master.codemaster.SystemControlledMasterCodes;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class LeadDocumentRepositoryWrapper {

    private final JdbcTemplate jdbcTemplate;
    private final CodeValueMasterService codeValueMasterService;
    private final ObjectMapper objectMapper;

    private final String sqlQueryForLeadDocuments = """
              SELECT
                  d.identifier,
                  d.name,
                  d.type,
                  d.size,
                  d.created_at as createdAt,
                  d.created_by as createdBy,
                  doc_detail->>'status' as status,
                  doc_detail->'tag' as tags
              FROM n_lead l
              CROSS JOIN LATERAL jsonb_array_elements(l.document_details) doc_detail
              JOIN n_document d ON (doc_detail->>'id')::bigint = d.id
            """;

    /**
     * Private static RowMapper for mapping ResultSet to LeadDocumentResponse.
     * Reusable across multiple query methods.
     */
    private record LeadDocumentRowMapper(CodeValueMasterService codeValueMasterService,
                                         ObjectMapper objectMapper) implements RowMapper<LeadDocumentResponse> {

        @Override
        public LeadDocumentResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            LeadDocumentResponse response = new LeadDocumentResponse();
            response.setIdentifier(UUID.fromString(rs.getString("identifier")));
            response.setName(rs.getString("name"));
            response.setType(rs.getString("type"));
            response.setSize(rs.getLong("size"));
            response.setCreatedAt(rs.getTimestamp("createdAt").toLocalDateTime());
            response.setCreatedBy(rs.getString("createdBy"));

            String statusStr = rs.getString("status");
            if (statusStr != null) {
                response.setStatus(LeadDocumentStatus.valueOf(statusStr));
            }

            String tagsJson = rs.getString("tags");
            if (tagsJson != null && !tagsJson.equals("null")) {
                try {
                    // Parse JSONB array to List<String> using Jackson
                    List<String> tags = objectMapper.readValue(tagsJson, new TypeReference<>() {});
                    List<CodeValueResponse> tagsCodes = codeValueMasterService.getByKeys(tags);
                    response.setTags(tagsCodes);
                } catch (Exception e) {
                    response.setTags(null);
                }
            }

            return response;
        }
    }

    /**
     * Get all documents for a lead by lead identifier with pagination.
     * Joins n_lead and n_document tables, extracts tags and status from Lead's documentDetails JSONB.
     */
    public PaginatedResponse<LeadDocumentResponse> findAllDocumentsByLeadIdentifier(
            UUID leadIdentifier, PaginationRequest paginationRequest) {
        
        int offset = paginationRequest.getOffset();
        int limit = paginationRequest.getLimit();
        String sortBy = paginationRequest.getSortBy();
        String sortDirection = paginationRequest.getSortDirection();
        
        // Count query
        String countSql = """
                SELECT COUNT(*)
                FROM n_lead l
                CROSS JOIN LATERAL jsonb_array_elements(l.document_details) doc_detail
                JOIN n_document d ON (doc_detail->>'id')::bigint = d.id
                WHERE l.lead_identifier = ?
                """;
        
        // Main query with sorting and pagination
        String sql = sqlQueryForLeadDocuments + 
                     " WHERE l.lead_identifier = ?" +
                     " ORDER BY d." + sortBy + " " + sortDirection +
                     " LIMIT ? OFFSET ?";

        try {
            // Get total count
            Long totalCount = jdbcTemplate.queryForObject(countSql, Long.class, leadIdentifier);
            long total = (totalCount != null) ? totalCount : 0L;
            
            // Get paginated data
            List<LeadDocumentResponse> documents = jdbcTemplate.query(
                    sql, new LeadDocumentRowMapper(codeValueMasterService, objectMapper), 
                    leadIdentifier, limit, offset);
            
            // Calculate pagination info
            int totalPages = (int) Math.ceil((double) total / limit);
            int currentPage = offset / limit;
            boolean hasNext = offset + limit < total;
            boolean hasPrevious = offset > 0;
            
            PaginationInfo paginationInfo = new PaginationInfo(
                    offset, limit, total, totalPages, currentPage, hasNext, hasPrevious);
            
            return new PaginatedResponse<>(documents, paginationInfo);
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to retrieve documents for lead: " + leadIdentifier, e);
        }
    }

    /**
     * Get a specific document for a lead by lead identifier and document identifier.
     */
    public LeadDocumentResponse findDocumentByLeadIdentifierAndDocumentIdentifier(
            UUID leadIdentifier, UUID documentIdentifier) {
        String sql = sqlQueryForLeadDocuments + " WHERE l.lead_identifier = ? AND d.identifier = ?";

        try {
            return jdbcTemplate.queryForObject(sql, new LeadDocumentRowMapper(codeValueMasterService, objectMapper),
                    leadIdentifier, documentIdentifier);
        } catch (EmptyResultDataAccessException e) {
            throw new RuntimeException("Document not found for lead: " + leadIdentifier +
                                       " with document identifier: " + documentIdentifier);
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to retrieve document for lead: " + leadIdentifier, e);
        }
    }
}

