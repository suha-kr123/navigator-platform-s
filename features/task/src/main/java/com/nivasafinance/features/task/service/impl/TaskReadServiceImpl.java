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
import com.nivasafinance.common.exception.ResourceNotFoundException;
import com.nivasafinance.features.task.service.TaskReadService;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true, noRollbackFor = ResourceNotFoundException.class)
@AllArgsConstructor
public class TaskReadServiceImpl implements TaskReadService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final CodeValueMasterService codeValueMasterService;
    
    // ApplicationContext to get self-proxy for calling enrichment method in separate transaction context
    @Autowired
    private ApplicationContext applicationContext;

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
        
        // Enrich responses with code master values in a separate transaction context
        // This prevents any exceptions from affecting the main transaction
        try {
            // Get self-proxy from application context to avoid circular dependency
            // This ensures the @Transactional annotation is respected through the proxy
            TaskReadServiceImpl selfProxy = applicationContext.getBean(TaskReadServiceImpl.class);
            selfProxy.enrichOutcomeValues(tasks);
        } catch (Exception e) {
            // Silently ignore enrichment errors - outcome keys will be used as fallback
            // This ensures the transaction is not affected by code master service issues
        }
        
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
                try {
                    taskDetails = objectMapper.convertValue(taskDetailsMap, TaskDetailsResponse.class);
                } catch (Exception e) {
                    // If conversion fails (e.g., entityId is not a valid UUID), manually construct TaskDetailsResponse
                    taskDetails = buildTaskDetailsResponseSafely(taskDetailsMap);
                }
            }
            
            UUID taskIdentifier = rs.getObject("task_identifier", UUID.class);
            
            // Store outcome key - will be enriched after query completes
            String outcomeKey = rs.getString("outcome");
            
            return TaskResponse.builder()
                    .taskIdentifier(taskIdentifier)
                    .taskConfigKey(rs.getString("task_config_key"))
                    .taskName(rs.getString("task_name"))
                    .taskDescription(rs.getString("task_description"))
                    .assignedTo(rs.getString("assigned_to"))
                    .dueAt(getLocalDateTime(rs, "due_at"))
                    .outcome(outcomeKey)
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
    
    /**
     * Safely builds TaskDetailsResponse when automatic conversion fails (e.g., entityId is not a valid UUID).
     */
    private TaskDetailsResponse buildTaskDetailsResponseSafely(Map<String, Object> taskDetailsMap) {
        UUID entityId = null;
        Object entityIdObj = taskDetailsMap.get("entityId");
        if (ValidationUtils.isNonNull(entityIdObj)) {
            if (entityIdObj instanceof UUID) {
                entityId = (UUID) entityIdObj;
            } else if (entityIdObj instanceof String) {
                try {
                    entityId = UUID.fromString((String) entityIdObj);
                } catch (IllegalArgumentException e) {
                    // If it's not a valid UUID string, leave it as null
                    entityId = null;
                }
            }
            // If entityIdObj is a number (like "15"), we can't convert it to UUID, so leave it as null
        }
        
        TaskDetailsResponse.PreferredCallWindow preferredCallWindow = null;
        @SuppressWarnings("unchecked")
        Map<String, Object> preferredCallWindowMap = (Map<String, Object>) taskDetailsMap.get("preferredCallWindow");
        if (ValidationUtils.isNonNull(preferredCallWindowMap) && !preferredCallWindowMap.isEmpty()) {
            try {
                preferredCallWindow = objectMapper.convertValue(preferredCallWindowMap, 
                    TaskDetailsResponse.PreferredCallWindow.class);
            } catch (Exception e) {
                // Fallback to manual parsing if convertValue fails
                Object start = preferredCallWindowMap.get("start");
                Object end = preferredCallWindowMap.get("end");
                if (ValidationUtils.isNonNull(start) || ValidationUtils.isNonNull(end)) {
                    preferredCallWindow = TaskDetailsResponse.PreferredCallWindow.builder()
                            .start(parseLocalDateTime(start))
                            .end(parseLocalDateTime(end))
                            .build();
                }
            }
        }
        
        Object iterCount = taskDetailsMap.get("iterationCount");
        Integer iterationCount = ValidationUtils.isNonNull(iterCount) ? 
                (iterCount instanceof Integer ? (Integer) iterCount : ((Number) iterCount).intValue()) : null;
        
        String entityTypeStr = (String) taskDetailsMap.get("entityType");
        com.nivasafinance.common.enums.EntityType entityType = null;
        if (ValidationUtils.isNonNullOrEmpty(entityTypeStr)) {
            try {
                entityType = com.nivasafinance.common.enums.EntityType.valueOf(entityTypeStr);
            } catch (IllegalArgumentException e) {
                // Invalid enum value, leave as null
            }
        }
        
        return TaskDetailsResponse.builder()
                .entityId(entityId)
                .entityType(entityType)
                .preferredCallWindow(preferredCallWindow)
                .creatorRemarks((String) taskDetailsMap.get("creatorRemarks"))
                .iterationCount(iterationCount)
                .build();
    }
    
    private LocalDateTime parseLocalDateTime(Object value) {
        if (!ValidationUtils.isNonNull(value)) {
            return null;
        }
        
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        
        if (value instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) value).toLocalDateTime();
        }
        
        if (value instanceof String) {
            try {
                String dateString = (String) value;
                if (dateString.contains("T")) {
                    return LocalDateTime.parse(dateString);
                }
                java.time.format.DateTimeFormatter formatter = 
                    java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                return LocalDateTime.parse(dateString, formatter);
            } catch (Exception e) {
                return null;
            }
        }
        
        return null;
    }
    
    /**
     * Enriches task responses with outcome values from code master.
     * This method runs with NOT_SUPPORTED propagation to suspend any existing transaction,
     * ensuring that exceptions from code master service won't affect the database transaction.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void enrichOutcomeValues(List<TaskResponse> responses) {
        if (responses == null || responses.isEmpty()) {
            return;
        }
        
        for (TaskResponse response : responses) {
            if (response == null) {
                continue;
            }
            
            String outcomeKey = response.getOutcome();
            if (ValidationUtils.isNonNullOrEmpty(outcomeKey)) {
                try {
                    CodeValueResponse outcomeCodeValue = codeValueMasterService.getByKey(outcomeKey);
                    if (ValidationUtils.isNonNull(outcomeCodeValue) && ValidationUtils.isNonNullOrEmpty(outcomeCodeValue.getValue())) {
                        response.setOutcome(outcomeCodeValue.getValue());
                    }
                } catch (ResourceNotFoundException e) {
                    // Expected - outcome key not found in code master, keep key as is
                } catch (RuntimeException e) {
                    // Any other runtime exception - keep key as is
                } catch (Exception e) {
                    // Any other exception - keep key as is
                }
            }
        }
    }
}

