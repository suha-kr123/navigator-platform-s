package com.nivasafinance.features.task.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.task.dto.TaskResponse;
import com.nivasafinance.features.task.service.TaskReadService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Implementation of TaskReadService using JDBC template for optimized read operations
 */
@Service
@Transactional(readOnly = true)
@AllArgsConstructor
@Slf4j
public class TaskReadServiceImpl implements TaskReadService {

    private final JdbcTemplate jdbcTemplate;
    private final MessageSource messageSource;
    private final ObjectMapper objectMapper;

    private static final String BASE_QUERY = 
        "SELECT " +
        "t.task_identifier, t.task_config_key, t.assigned_to, t.assigned_to_role, " +
        "t.due_at, t.outcome, t.outcome_details, t.task_details, " +
        "t.created_at, t.created_by, t.updated_at, t.updated_by, " +
        "tc.name as task_name, tc.description as task_description " +
        "FROM n_tasks t " +
        "LEFT JOIN n_task_config tc ON t.task_config_key = tc.task_config_key ";

    private static final String COUNT_QUERY_PREFIX = "SELECT COUNT(*) FROM n_tasks t ";

    @Override
    public PaginatedResponse<TaskResponse> getTasksByAssignedTo(String assignedTo, boolean includeCompleted, 
                                                                 PaginationRequest paginationRequest) {
        log.debug("Fetching tasks for assignedTo: {}, includeCompleted: {}, pagination: {}", 
                  assignedTo, includeCompleted, paginationRequest);
        
        // Build WHERE clause
        StringBuilder whereClause = new StringBuilder("WHERE t.assigned_to = ? ");
        if (!includeCompleted) {
            whereClause.append("AND t.outcome IS NULL ");
        }
        
        // Get total count
        String countQuery = COUNT_QUERY_PREFIX + whereClause;
        Long totalElements = jdbcTemplate.queryForObject(countQuery, Long.class, assignedTo);
        totalElements = totalElements != null ? totalElements : 0L;
        
        // Build ORDER BY clause
        String orderBy = buildOrderByClause(paginationRequest);
        
        // Build main query with pagination
        String query = BASE_QUERY + whereClause + orderBy + " LIMIT ? OFFSET ?";
        
        List<TaskResponse> tasks = jdbcTemplate.query(
            query, 
            new TaskRowMapper(), 
            assignedTo, 
            paginationRequest.getLimit(), 
            paginationRequest.getOffset()
        );
        
        return buildPaginatedResponse(tasks, paginationRequest, totalElements);
    }

    @Override
    public PaginatedResponse<TaskResponse> getTasksByAssignedToRole(String assignedToRole, boolean includeCompleted, 
                                                                     PaginationRequest paginationRequest) {
        log.debug("Fetching tasks for assignedToRole: {}, includeCompleted: {}, pagination: {}", 
                  assignedToRole, includeCompleted, paginationRequest);
        
        // Build WHERE clause
        StringBuilder whereClause = new StringBuilder("WHERE t.assigned_to_role = ? ");
        if (!includeCompleted) {
            whereClause.append("AND t.outcome IS NULL ");
        }
        
        // Get total count
        String countQuery = COUNT_QUERY_PREFIX + whereClause;
        Long totalElements = jdbcTemplate.queryForObject(countQuery, Long.class, assignedToRole);
        totalElements = totalElements != null ? totalElements : 0L;
        
        // Build ORDER BY clause
        String orderBy = buildOrderByClause(paginationRequest);
        
        // Build main query with pagination
        String query = BASE_QUERY + whereClause + orderBy + " LIMIT ? OFFSET ?";
        
        List<TaskResponse> tasks = jdbcTemplate.query(
            query, 
            new TaskRowMapper(), 
            assignedToRole, 
            paginationRequest.getLimit(), 
            paginationRequest.getOffset()
        );
        
        return buildPaginatedResponse(tasks, paginationRequest, totalElements);
    }

    /**
     * Build ORDER BY clause based on pagination request
     */
    private String buildOrderByClause(PaginationRequest paginationRequest) {
        String sortBy = paginationRequest.getSortBy();
        String sortDirection = paginationRequest.getSortDirection();
        
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "ORDER BY t.due_at ASC, t.created_at DESC ";
        }
        
        // Validate and map sort field
        String column = switch (sortBy) {
            case "id" -> "t.id";
            case "taskConfigKey" -> "t.task_config_key";
            case "assignedTo" -> "t.assigned_to";
            case "assignedToRole" -> "t.assigned_to_role";
            case "dueAt" -> "t.due_at";
            case "outcome" -> "t.outcome";
            case "createdAt" -> "t.created_at";
            case "updatedAt" -> "t.updated_at";
            default -> "t.due_at"; // Default to dueAt
        };
        
        String direction = "DESC".equalsIgnoreCase(sortDirection) ? "DESC" : "ASC";
        return String.format("ORDER BY %s %s, t.created_at DESC ", column, direction);
    }

    /**
     * Build paginated response with pagination info
     */
    private PaginatedResponse<TaskResponse> buildPaginatedResponse(List<TaskResponse> tasks, 
                                                                    PaginationRequest paginationRequest, 
                                                                    long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / paginationRequest.getLimit());
        int currentPage = paginationRequest.getOffset() / paginationRequest.getLimit();
        boolean hasNext = (paginationRequest.getOffset() + paginationRequest.getLimit()) < totalElements;
        boolean hasPrevious = paginationRequest.getOffset() > 0;
        
        PaginationInfo paginationInfo = new PaginationInfo(
            paginationRequest.getOffset(),
            paginationRequest.getLimit(),
            totalElements,
            totalPages,
            currentPage,
            hasNext,
            hasPrevious
        );
        
        return new PaginatedResponse<>(tasks, paginationInfo);
    }

    /**
     * Row mapper for mapping result set to TaskResponse
     */
    private class TaskRowMapper implements RowMapper<TaskResponse> {
        @Override
        public TaskResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            return TaskResponse.builder()
                    .taskIdentifier(rs.getString("task_identifier"))
                    .taskConfigKey(rs.getString("task_config_key"))
                    .taskName(rs.getString("task_name"))
                    .taskDescription(rs.getString("task_description"))
                    .assignedTo(rs.getString("assigned_to"))
                    .assignedToRole(rs.getString("assigned_to_role"))
                    .dueAt(getLocalDateTime(rs, "due_at"))
                    .outcome(rs.getString("outcome"))
                    .outcomeDetails(parseJsonColumn(rs, "outcome_details"))
                    .taskDetails(parseJsonColumn(rs, "task_details"))
                    .createdAt(getLocalDateTime(rs, "created_at"))
                    .createdBy(rs.getString("created_by"))
                    .updatedAt(getLocalDateTime(rs, "updated_at"))
                    .updatedBy(rs.getString("updated_by"))
                    .build();
        }
    }

    /**
     * Safely extract UUID from ResultSet
     */
    private java.util.UUID getUuid(ResultSet rs, String columnName) throws SQLException {
        Object value = rs.getObject(columnName);
        if (value == null) {
            return null;
        }
        
        if (value instanceof java.util.UUID) {
            return (java.util.UUID) value;
        }
        
        if (value instanceof String) {
            return java.util.UUID.fromString((String) value);
        }
        
        return null;
    }

    /**
     * Safely extract LocalDateTime from ResultSet
     */
    private LocalDateTime getLocalDateTime(ResultSet rs, String columnName) throws SQLException {
        Object value = rs.getObject(columnName);
        if (value == null) {
            return null;
        }
        
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        
        if (value instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) value).toLocalDateTime();
        }
        
        return null;
    }

    /**
     * Parse JSONB column to Map
     */
    private Map<String, Object> parseJsonColumn(ResultSet rs, String columnName) {
        try {
            String jsonString = rs.getString(columnName);
            if (jsonString == null || jsonString.trim().isEmpty()) {
                return null;
            }
            return objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse JSON column {}: {}", columnName, e.getMessage());
            return null;
        }
    }
}

