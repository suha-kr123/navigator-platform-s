package com.nivasafinance.features.task.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.task.dto.OutcomeDetailsResponse;
import com.nivasafinance.features.task.dto.TaskDetailsResponse;
import com.nivasafinance.features.task.dto.TaskResponse;
import com.nivasafinance.features.task.service.TaskReadService;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class TaskReadServiceImpl implements TaskReadService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    private static final String BASE_QUERY = 
        "SELECT " +
        "t.task_identifier, t.task_config_key, t.assigned_to, " +
        "t.due_at, t.outcome, t.outcome_details, t.task_details, " +
        "t.created_at, t.created_by, t.updated_at, t.updated_by, " +
        "tc.name as task_name, tc.description as task_description " +
        "FROM n_tasks t " +
        "LEFT JOIN n_task_config tc ON t.task_config_key = tc.task_config_key ";

    private static final String COUNT_QUERY_PREFIX = "SELECT COUNT(*) FROM n_tasks t ";

    @Override
    public PaginatedResponse<TaskResponse> getTasksByAssignedTo(String assignedTo, boolean includeCompleted, 
                                                                 PaginationRequest paginationRequest) {
        StringBuilder whereClause = new StringBuilder("WHERE t.assigned_to = ? ");
        if (!includeCompleted) {
            whereClause.append("AND t.outcome IS NULL ");
        }
        
        String countQuery = COUNT_QUERY_PREFIX + whereClause;
        Long totalElements = jdbcTemplate.queryForObject(countQuery, Long.class, assignedTo);
        totalElements = totalElements != null ? totalElements : 0L;
        
        String orderBy = buildOrderByClause(paginationRequest);
        
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

    private String buildOrderByClause(PaginationRequest paginationRequest) {
        String sortBy = paginationRequest.getSortBy();
        String sortDirection = paginationRequest.getSortDirection();
        
        if (!ValidationUtils.isNonNull(sortBy) || sortBy.trim().isEmpty()) {
            return "ORDER BY t.due_at ASC, t.created_at DESC ";
        }
        
        String column = switch (sortBy) {
            case "id" -> "t.id";
            case "taskConfigKey" -> "t.task_config_key";
            case "assignedTo" -> "t.assigned_to";
            case "dueAt" -> "t.due_at";
            case "outcome" -> "t.outcome";
            case "createdAt" -> "t.created_at";
            case "updatedAt" -> "t.updated_at";
            default -> "t.due_at";
        };
        
        String direction = "DESC".equalsIgnoreCase(sortDirection) ? "DESC" : "ASC";
        return String.format("ORDER BY %s %s, t.created_at DESC ", column, direction);
    }

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

    private class TaskRowMapper implements RowMapper<TaskResponse> {
        @Override
        public TaskResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            Map<String, Object> outcomeDetailsMap = parseJsonColumn(rs, "outcome_details");
            OutcomeDetailsResponse outcomeDetails = null;
            if (ValidationUtils.isNonNull(outcomeDetailsMap) && !outcomeDetailsMap.isEmpty()) {
                outcomeDetails = objectMapper.convertValue(outcomeDetailsMap, OutcomeDetailsResponse.class);
            }
            
            Map<String, Object> taskDetailsMap = parseJsonColumn(rs, "task_details");
            TaskDetailsResponse taskDetails = null;
            if (ValidationUtils.isNonNull(taskDetailsMap) && !taskDetailsMap.isEmpty()) {
                taskDetails = objectMapper.convertValue(taskDetailsMap, TaskDetailsResponse.class);
            }
            
            UUID taskIdentifier = rs.getObject("task_identifier", UUID.class);
            return TaskResponse.builder()
                    .taskIdentifier(taskIdentifier)
                    .taskConfigKey(rs.getString("task_config_key"))
                    .taskName(rs.getString("task_name"))
                    .taskDescription(rs.getString("task_description"))
                    .assignedTo(rs.getString("assigned_to"))
                    .dueAt(getLocalDateTime(rs, "due_at"))
                    .outcome(rs.getString("outcome"))
                    .outcomeDetails(outcomeDetails)
                    .taskDetails(taskDetails)
                    .createdAt(getLocalDateTime(rs, "created_at"))
                    .createdBy(rs.getString("created_by"))
                    .updatedAt(getLocalDateTime(rs, "updated_at"))
                    .updatedBy(rs.getString("updated_by"))
                    .build();
        }
    }

    private LocalDateTime getLocalDateTime(ResultSet rs, String columnName) throws SQLException {
        Object value = rs.getObject(columnName);
        if (!ValidationUtils.isNonNull(value)) {
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

    private Map<String, Object> parseJsonColumn(ResultSet rs, String columnName) {
        try {
            String jsonString = rs.getString(columnName);
            if (!ValidationUtils.isNonNull(jsonString) || jsonString.trim().isEmpty()) {
                return null;
            }
            return objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return null;
        }
    }
}

