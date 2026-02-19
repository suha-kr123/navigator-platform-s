package com.nivasafinance.features.lead.repository;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lead.dto.LeadNoteResponse;
import com.nivasafinance.features.notes.exception.NotesNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
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
public class LeadNoteRepositoryWrapper {

    private final JdbcTemplate jdbcTemplate;

    private final String sqlQueryForLeadNotes = """
              SELECT
                  n.id,
                  n.identifier,
                  n.title,
                  n.content,
                  n.created_at as createdAt,
                  n.updated_at as updatedAt,
                  n.created_by as createdBy,
                  n.updated_by as updatedBy
              FROM n_lead l
              CROSS JOIN LATERAL  jsonb_array_elements(l.notes) note_id
              JOIN n_note n ON (note_id#>>'{}')::bigint = n.id
            """;
    private final MessageSource messageSource;

    /**
     * Private static RowMapper for mapping ResultSet to LeadNoteResponse.
     * Reusable across multiple query methods.
     */
    private record LeadNoteRowMapper() implements RowMapper<LeadNoteResponse> {

        @Override
        public LeadNoteResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            LeadNoteResponse response = new LeadNoteResponse();
            response.setIdentifier(UUID.fromString(rs.getString("identifier")));
            response.setTitle(rs.getString("title"));
            response.setContent(rs.getString("content"));
            response.setCreatedAt(rs.getTimestamp("createdAt").toLocalDateTime());
            response.setUpdatedAt(rs.getTimestamp("updatedAt").toLocalDateTime());
            response.setCreatedBy(rs.getString("createdBy"));
            response.setUpdatedBy(rs.getString("updatedBy"));
            return response;
        }
    }

    /**
     * Get all notes for a lead by lead identifier with pagination.
     * Joins n_lead and n_note tables using the notes array.
     */
    public PaginatedResponse<LeadNoteResponse> findAllNotesByLeadIdentifier(
            UUID leadIdentifier, PaginationRequest paginationRequest) {
        
        int offset = paginationRequest.getOffset();
        int limit = paginationRequest.getLimit();
        String sortBy = mapSortByToColumn(paginationRequest.getSortBy());
        String sortDirection = paginationRequest.getSortDirection() != null ? paginationRequest.getSortDirection() : "DESC";
        
        // Count query
        String countSql = """
                SELECT COUNT(*)
                FROM n_lead l
                CROSS JOIN LATERAL jsonb_array_elements(l.notes) note_id
                JOIN n_note n ON (note_id#>>'{}')::bigint = n.id
                WHERE l.lead_identifier = ?
                """;
        
        // Main query with sorting and pagination
        String sql = sqlQueryForLeadNotes + 
                     " WHERE l.lead_identifier = ?" +
                     " ORDER BY n." + sortBy + " " + sortDirection +
                     " LIMIT ? OFFSET ?";

        try {
            // Get total count
            Long totalCount = jdbcTemplate.queryForObject(countSql, Long.class, leadIdentifier);
            long total = (totalCount != null) ? totalCount : 0L;
            
            // Get paginated data
            List<LeadNoteResponse> notes = jdbcTemplate.query(
                    sql, new LeadNoteRowMapper(), leadIdentifier, limit, offset);
            
            // Calculate pagination info
            int totalPages = (int) Math.ceil((double) total / limit);
            int currentPage = offset / limit;
            boolean hasNext = offset + limit < total;
            boolean hasPrevious = offset > 0;
            
            PaginationInfo paginationInfo = new PaginationInfo(
                    offset, limit, total, totalPages, currentPage, hasNext, hasPrevious);
            
            return new PaginatedResponse<>(notes, paginationInfo);
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to retrieve notes for lead: " + leadIdentifier, e);
        }
    }

    private static String mapSortByToColumn(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "created_at";
        }
        return switch (sortBy) {
            case "createdAt" -> "created_at";
            case "updatedAt" -> "updated_at";
            case "createdBy" -> "created_by";
            case "updatedBy" -> "updated_by";
            case "title", "content" -> sortBy;
            default -> "created_at";
        };
    }

    /**
     * Get a specific note for a lead by lead identifier and note identifier.
     */
    public LeadNoteResponse findNoteByLeadIdentifierAndNoteIdentifier(
            UUID leadIdentifier, UUID noteIdentifier) {
        String sql = sqlQueryForLeadNotes + " WHERE l.lead_identifier = ? AND n.identifier = ?";

        try {
            return jdbcTemplate.queryForObject(sql, new LeadNoteRowMapper(),
                    leadIdentifier, noteIdentifier);
        } catch (EmptyResultDataAccessException e) {
            throw new NotesNotFoundException(noteIdentifier, messageSource);
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to retrieve note for lead: " + leadIdentifier, e);
        }
    }
}

