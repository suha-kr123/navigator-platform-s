package com.nivasafinance.features.task.repository.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.task.dto.OutcomeDetailsResponse;
import com.nivasafinance.features.task.dto.TaskDetailsResponse;
import com.nivasafinance.features.task.dto.TaskResponse;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class TaskResponseRowMapper implements RowMapper<TaskResponse> {
    private final ObjectMapper objectMapper;

    public TaskResponseRowMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

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
                taskDetails = buildTaskDetailsResponseSafely(taskDetailsMap);
            }
        }

        UUID taskIdentifier = rs.getObject("task_identifier", UUID.class);
        String outcomeKey = rs.getString("outcome");
        String taskName = rs.getString("name");
        String taskConfigName = rs.getString("task_config_name");
        String finalTaskName = ValidationUtils.isNonNull(taskName) ? taskName : taskConfigName;

        return TaskResponse.builder()
                .taskIdentifier(taskIdentifier)
                .taskConfigKey(rs.getString("task_config_key"))
                .taskName(finalTaskName)
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
                    entityId = null;
                }
            }
        }

        TaskDetailsResponse.PreferredCallWindow preferredCallWindow = null;
        @SuppressWarnings("unchecked")
        Map<String, Object> preferredCallWindowMap = (Map<String, Object>) taskDetailsMap.get("preferredCallWindow");
        if (ValidationUtils.isNonNull(preferredCallWindowMap) && !preferredCallWindowMap.isEmpty()) {
            try {
                preferredCallWindow = objectMapper.convertValue(preferredCallWindowMap,
                        TaskDetailsResponse.PreferredCallWindow.class);
            } catch (Exception e) {
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
        EntityType entityType = null;
        if (ValidationUtils.isNonNullOrEmpty(entityTypeStr)) {
            try {
                entityType = EntityType.valueOf(entityTypeStr);
            } catch (IllegalArgumentException e) {
            }
        }

        UUID rescheduledFromTaskIdentifier = null;
        Object rescheduledFromTaskIdObj = taskDetailsMap.get("rescheduledFromTaskIdentifier");
        if (ValidationUtils.isNonNull(rescheduledFromTaskIdObj)) {
            if (rescheduledFromTaskIdObj instanceof UUID) {
                rescheduledFromTaskIdentifier = (UUID) rescheduledFromTaskIdObj;
            } else if (rescheduledFromTaskIdObj instanceof String) {
                try {
                    rescheduledFromTaskIdentifier = UUID.fromString((String) rescheduledFromTaskIdObj);
                } catch (IllegalArgumentException e) {
                    rescheduledFromTaskIdentifier = null;
                }
            }
        }

        String rescheduleReasonCodeValueKey = (String) taskDetailsMap.get("rescheduleReasonCodeValueKey");
        String rescheduledFromTaskRemarks = (String) taskDetailsMap.get("rescheduledFromTaskRemarks");

        return TaskDetailsResponse.builder()
                .entityId(entityId)
                .entityType(entityType)
                .stageKey((String) taskDetailsMap.get("stageKey"))
                .preferredCallWindow(preferredCallWindow)
                .creatorRemarks((String) taskDetailsMap.get("creatorRemarks"))
                .iterationCount(iterationCount)
                .rescheduledFromTaskIdentifier(rescheduledFromTaskIdentifier)
                .rescheduleReasonCodeValueKey(rescheduleReasonCodeValueKey)
                .rescheduledFromTaskRemarks(rescheduledFromTaskRemarks)
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
}
