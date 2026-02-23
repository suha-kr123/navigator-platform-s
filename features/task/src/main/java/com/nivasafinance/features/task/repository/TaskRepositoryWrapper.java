package com.nivasafinance.features.task.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.features.task.entity.Task;
import com.nivasafinance.features.task.dto.TaskResponse;
import com.nivasafinance.features.task.exception.TaskNotFoundException;
import com.nivasafinance.features.task.exception.TaskOperationException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.jdbc.core.JdbcTemplate;
import com.nivasafinance.features.task.repository.mapper.TaskResponseRowMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class TaskRepositoryWrapper {

    private final TaskRepository taskRepository;
    private final MessageSource messageSource;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public Task saveWithException(Task task) {
        try {
            return taskRepository.save(task);
        } catch (RuntimeException e) {
            throw TaskOperationException.taskConfigInactive(task.getTaskConfigKey(), messageSource);
        }
    }

    public Task findByTaskIdentifierWithException(UUID taskIdentifier) {
        return taskRepository.findByTaskIdentifier(taskIdentifier)
                .orElseThrow(() -> TaskNotFoundException.taskNotFoundByUuid(taskIdentifier, messageSource));
    }

    public Optional<Task> findById(Long id) {
        return taskRepository.findById(id);
    }

    public List<Task> findOpenTasksByLeadIdentifier(UUID leadIdentifier) {
        return taskRepository.findOpenTasksByEntityIdAndEntityType(
                leadIdentifier,
                com.nivasafinance.common.enums.EntityType.LEAD.name());
    }

    private static final String BASE_QUERY = "SELECT " +
            "t.task_identifier, t.task_config_key, t.name, t.assigned_to, " +
            "t.due_at, t.outcome, t.outcome_details, t.task_details, " +
            "t.created_at, t.created_by, t.updated_at, t.updated_by, " +
            "tc.name as task_config_name, tc.description as task_description " +
            "FROM n_tasks t " +
            "LEFT JOIN n_task_config tc ON t.task_config_key = tc.task_config_key AND tc.is_active = true ";

    private static final String COUNT_QUERY_PREFIX = "SELECT COUNT(*) FROM n_tasks t ";

    public Long countTasks() {
        Long total = jdbcTemplate.queryForObject(COUNT_QUERY_PREFIX, Long.class);
        return total != null ? total : 0L;
    }

    public Long countTasksByAssignedTo(String assignedTo, boolean includeCompleted) {
        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder("WHERE t.assigned_to = ? ");
        params.add(assignedTo);
        if (!includeCompleted) {
            where.append("AND t.outcome IS NULL ");
        }
        String countQuery = COUNT_QUERY_PREFIX + where;
        Long total = jdbcTemplate.queryForObject(countQuery, Long.class, params.toArray());
        return total != null ? total : 0L;
    }

    public java.util.List<TaskResponse> findTasksByAssignedTo(String assignedTo, boolean includeCompleted,
            PaginationRequest paginationRequest) {
        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder("WHERE t.assigned_to = ? ");
        params.add(assignedTo);
        if (!includeCompleted) {
            where.append("AND t.outcome IS NULL ");
        }
        String orderBy = buildOrderByClause(paginationRequest);
        params.add(paginationRequest.getLimit());
        params.add(paginationRequest.getOffset());
        String query = BASE_QUERY + where + orderBy + " LIMIT ? OFFSET ?";
        return jdbcTemplate.query(query, new TaskResponseRowMapper(objectMapper), params.toArray());
    }

    public java.util.List<TaskResponse> findAdhocTasksByEntity(EntityType entityType, UUID entityId) {
        StringBuilder sb = new StringBuilder();
        sb.append(BASE_QUERY);
        sb.append("WHERE (t.task_details->>'entityType') = ? ");
        sb.append("AND (t.task_details->>'entityId')::uuid = ? ");
        sb.append("AND COALESCE(tc.task_config_details->>'isAdhocTaskAllowed', 'false') = 'true' ");
        sb.append("ORDER BY t.created_at DESC");
        return jdbcTemplate.query(sb.toString(), new TaskResponseRowMapper(objectMapper), entityType.name(), entityId);
    }

    public Long countTasksByEntity(EntityType entityType, UUID entityId) {
        List<Object> params = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append(COUNT_QUERY_PREFIX);
        sb.append("WHERE (t.task_details->>'entityType') = ? AND (t.task_details->>'entityId')::uuid = ? ");
        params.add(entityType.name());
        params.add(entityId);
        Long total = jdbcTemplate.queryForObject(sb.toString(), Long.class, params.toArray());
        return total != null ? total : 0L;
    }

    public java.util.List<TaskResponse> findTasksByEntity(EntityType entityType, UUID entityId,
            PaginationRequest paginationRequest) {
        List<Object> params = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append(BASE_QUERY);
        sb.append("WHERE (t.task_details->>'entityType') = ? AND (t.task_details->>'entityId')::uuid = ? ");
        sb.append(buildOrderByClause(paginationRequest));
        sb.append(" LIMIT ? OFFSET ?");
        params.add(entityType.name());
        params.add(entityId);
        params.add(paginationRequest.getLimit());
        params.add(paginationRequest.getOffset());
        return jdbcTemplate.query(sb.toString(), new TaskResponseRowMapper(objectMapper), params.toArray());
    }

    public java.util.List<TaskResponse> findAllTasks(PaginationRequest paginationRequest) {
        String orderBy = buildOrderByClause(paginationRequest);
        String query = BASE_QUERY + orderBy + " LIMIT ? OFFSET ?";
        return jdbcTemplate.query(query, new TaskResponseRowMapper(objectMapper),
                paginationRequest.getLimit(), paginationRequest.getOffset());
    }

    private String buildOrderByClause(PaginationRequest paginationRequest) {
        String sortBy = paginationRequest.getSortBy();
        String sortDirection = paginationRequest.getSortDirection();
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "ORDER BY t.due_at ASC, t.created_at DESC ";
        }
        String column;
        switch (sortBy) {
            case "id":
                column = "t.id";
                break;
            case "taskConfigKey":
                column = "t.task_config_key";
                break;
            case "assignedTo":
                column = "t.assigned_to";
                break;
            case "dueAt":
                column = "t.due_at";
                break;
            case "outcome":
                column = "t.outcome";
                break;
            case "createdAt":
                column = "t.created_at";
                break;
            case "updatedAt":
                column = "t.updated_at";
                break;
            default:
                column = "t.due_at";
        }
        String direction = "DESC".equalsIgnoreCase(sortDirection) ? "DESC" : "ASC";
        return String.format("ORDER BY %s %s, t.created_at DESC ", column, direction);
    }

    private PaginatedResponse<TaskResponse> buildPaginatedResponse(
            java.util.List<TaskResponse> tasks,
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

    public PaginatedResponse<TaskResponse> findTasksByAssignedToPaginated(
            String assignedTo, boolean includeCompleted, PaginationRequest paginationRequest) {
        Long totalElements = countTasksByAssignedTo(assignedTo, includeCompleted);
        java.util.List<TaskResponse> tasks = findTasksByAssignedTo(assignedTo, includeCompleted, paginationRequest);
        return buildPaginatedResponse(tasks, paginationRequest, totalElements);
    }

    public PaginatedResponse<TaskResponse> findTasksByEntityPaginated(
            EntityType entityType, UUID entityId, PaginationRequest paginationRequest) {
        Long totalElements = countTasksByEntity(entityType, entityId);
        java.util.List<TaskResponse> tasks = findTasksByEntity(entityType, entityId, paginationRequest);
        return buildPaginatedResponse(tasks, paginationRequest, totalElements);
    }

    public PaginatedResponse<TaskResponse> findAllTasksPaginated(PaginationRequest paginationRequest) {
        Long totalElements = countTasks();
        java.util.List<TaskResponse> tasks = findAllTasks(paginationRequest);
        return buildPaginatedResponse(tasks, paginationRequest, totalElements);
    }
}
