package com.nivasafinance.features.advisor.repository;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.advisor.dto.AdvisorNoteResponse;
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
public class AdvisorNoteRepositoryWrapper {

    private final JdbcTemplate jdbcTemplate;

    private final String sqlQueryForAdvisorNotes = """
              SELECT
                  n.id,
                  n.identifier,
                  n.title,
                  n.content,
                  n.created_at as createdAt,
                  n.updated_at as updatedAt,
                  n.created_by as createdBy,
                  n.updated_by as updatedBy
              FROM n_advisor a
              CROSS JOIN LATERAL  jsonb_array_elements(a.notes) note_id
              JOIN n_note n ON (note_id#>>'{}')::bigint = n.id
            """;

    /**
     * Private static RowMapper for mapping ResultSet to AdvisorNoteResponse.
     * Reusable across multiple query methods.
     */
    private record AdvisorNoteRowMapper() implements RowMapper<AdvisorNoteResponse> {

        @Override
        public AdvisorNoteResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            AdvisorNoteResponse response = new AdvisorNoteResponse();
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
     * Get all notes for an advisor by advisor identifier with pagination.
     * Joins n_advisor and n_note tables using the notes array.
     */
    public PaginatedResponse<AdvisorNoteResponse> findAllNotesByAdvisorIdentifier(
            UUID advisorIdentifier, PaginationRequest paginationRequest) {
        
        int offset = paginationRequest.getOffset();
        int limit = paginationRequest.getLimit();
        String sortBy = mapSortByToColumn(paginationRequest.getSortBy());
        String sortDirection = paginationRequest.getSortDirection() != null ? paginationRequest.getSortDirection() : "DESC";
        
        // Count query
        String countSql = """
                SELECT COUNT(*)
                FROM n_advisor a
                CROSS JOIN LATERAL jsonb_array_elements(a.notes) note_id
                JOIN n_note n ON (note_id#>>'{}')::bigint = n.id
                WHERE a.identifier = ?
                """;
        
        // Main query with sorting and pagination
        String sql = sqlQueryForAdvisorNotes + 
                     " WHERE a.identifier = ?" +
                     " ORDER BY n." + sortBy + " " + sortDirection +
                     " LIMIT ? OFFSET ?";

        try {
            // Get total count
            Long totalCount = jdbcTemplate.queryForObject(countSql, Long.class, advisorIdentifier);
            long total = (totalCount != null) ? totalCount : 0L;
            
            // Get paginated data
            List<AdvisorNoteResponse> notes = jdbcTemplate.query(
                    sql, new AdvisorNoteRowMapper(), advisorIdentifier, limit, offset);
            
            // Calculate pagination info
            int totalPages = (int) Math.ceil((double) total / limit);
            int currentPage = offset / limit;
            boolean hasNext = offset + limit < total;
            boolean hasPrevious = offset > 0;
            
            PaginationInfo paginationInfo = new PaginationInfo(
                    offset, limit, total, totalPages, currentPage, hasNext, hasPrevious);
            
            return new PaginatedResponse<>(notes, paginationInfo);
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to retrieve notes for advisor: " + advisorIdentifier, e);
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
     * Get a specific note for an advisor by advisor identifier and note identifier.
     */
    public AdvisorNoteResponse findNoteByAdvisorIdentifierAndNoteIdentifier(
            UUID advisorIdentifier, UUID noteIdentifier) {
        String sql = sqlQueryForAdvisorNotes + " WHERE a.identifier = ? AND n.identifier = ?";

        try {
            return jdbcTemplate.queryForObject(sql, new AdvisorNoteRowMapper(),
                    advisorIdentifier, noteIdentifier);
        } catch (EmptyResultDataAccessException e) {
            throw new RuntimeException("Note not found for advisor: " + advisorIdentifier +
                                       " with note identifier: " + noteIdentifier);
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to retrieve note for advisor: " + advisorIdentifier, e);
        }
    }
}

