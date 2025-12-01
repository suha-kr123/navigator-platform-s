package com.nivasafinance.features.leadtasks.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.leadstages.repository.LeadStageHistoryRepositoryWrapper;
import com.nivasafinance.features.leadstages.entity.LeadStageHistory;
import com.nivasafinance.features.leadtasks.dto.LeadTaskResponse;
import com.nivasafinance.features.leadtasks.service.LeadTaskReadService;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import com.nivasafinance.features.workflow.orchestrator.WorkflowOrchestratorService;
import lombok.RequiredArgsConstructor;
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
import java.util.*;

@Service
@Transactional(readOnly = true, noRollbackFor = ResourceNotFoundException.class)
@RequiredArgsConstructor
public class LeadTaskReadServiceImpl implements LeadTaskReadService {

    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final LeadStageHistoryRepositoryWrapper leadStageHistoryRepositoryWrapper;
    private final WorkflowOrchestratorService workflowOrchestratorService;
    private final CodeValueMasterService codeValueMasterService;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    
    // ApplicationContext to get self-proxy for calling enrichment method in separate transaction context
    @Autowired
    private ApplicationContext applicationContext;

    private static final String BASE_QUERY = 
        "SELECT " +
        "lt.id as lead_task_id, lt.task_details as lead_task_details, " +
        "lt.created_at as lead_task_created_at, lt.created_by as lead_task_created_by, " +
        "lt.updated_at as lead_task_updated_at, lt.updated_by as lead_task_updated_by, " +
        "t.task_identifier, t.task_config_key, t.assigned_to, " +
        "t.due_at, t.outcome, t.outcome_details, t.task_details, " +
        "t.created_at, t.created_by, t.updated_at, t.updated_by, " +
        "tc.name as task_name, tc.description as task_description " +
        "FROM n_lead_tasks lt " +
        "INNER JOIN n_tasks t ON lt.task_id = t.id " +
        "LEFT JOIN n_task_config tc ON t.task_config_key = tc.task_config_key AND tc.is_active = true ";

    @Override
    public PaginatedResponse<LeadTaskResponse> getTasksByLeadId(UUID leadIdentifier, PaginationRequest paginationRequest) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        
        String whereClause = "WHERE lt.lead_id = ? ";
        String countQuery = "SELECT COUNT(*) FROM n_lead_tasks lt INNER JOIN n_tasks t ON lt.task_id = t.id " + whereClause;
        
        Long totalElements = jdbcTemplate.queryForObject(countQuery, Long.class, lead.getId());
        totalElements = ValidationUtils.isNonNull(totalElements) ? totalElements : 0L;
        
        String orderBy = "ORDER BY lt.created_at DESC ";
        String query = BASE_QUERY + whereClause + orderBy + "LIMIT ? OFFSET ?";
        
        List<LeadTaskResponse> responses = jdbcTemplate.query(
            query,
            new LeadTaskResponseRowMapper(lead.getLeadIdentifier()),
            lead.getId(),
            paginationRequest.getLimit(),
            paginationRequest.getOffset()
        );
        
        PaginationInfo paginationInfo = buildPaginationInfo(paginationRequest, totalElements);
        PaginatedResponse<LeadTaskResponse> paginatedResponse = new PaginatedResponse<>(responses, paginationInfo);
        
        // Enrich responses with code master values in a separate transaction context
        // This prevents any exceptions from affecting the main transaction
        try {
            // Get self-proxy from application context to avoid circular dependency
            // This ensures the @Transactional annotation is respected through the proxy
            LeadTaskReadServiceImpl selfProxy = applicationContext.getBean(LeadTaskReadServiceImpl.class);
            selfProxy.enrichOutcomeValues(responses);
        } catch (Exception e) {
            // Silently ignore enrichment errors - outcome keys will be used as fallback
            // This ensures the transaction is not affected by code master service issues
        }
        
        return paginatedResponse;
    }
    
    /**
     * Enriches task responses with outcome values and reschedule reason values from code master.
     * This method runs with NOT_SUPPORTED propagation to suspend any existing transaction,
     * ensuring that exceptions from code master service won't affect the database transaction.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void enrichOutcomeValues(List<LeadTaskResponse> responses) {
        if (responses == null || responses.isEmpty()) {
            return;
        }
        
        for (LeadTaskResponse response : responses) {
            if (response == null) {
                continue;
            }
            
            // Enrich outcome value
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
            
            // Enrich reschedule reason code value key
            LeadTaskResponse.OutcomeDetails outcomeDetails = response.getOutcomeDetails();
            if (outcomeDetails != null && ValidationUtils.isNonNullOrEmpty(outcomeDetails.getRescheduleReasonCodeValueKey())) {
                try {
                    CodeValueResponse rescheduleReasonCodeValue = codeValueMasterService.getByKey(
                            outcomeDetails.getRescheduleReasonCodeValueKey());
                    if (ValidationUtils.isNonNull(rescheduleReasonCodeValue) && 
                            ValidationUtils.isNonNullOrEmpty(rescheduleReasonCodeValue.getValue())) {
                        outcomeDetails.setRescheduleReasonCodeValueKey(rescheduleReasonCodeValue.getValue());
                    }
                } catch (ResourceNotFoundException e) {
                    // Expected - reschedule reason key not found in code master, keep key as is
                } catch (RuntimeException e) {
                    // Any other runtime exception - keep key as is
                } catch (Exception e) {
                    // Any other exception - keep key as is
                }
            }
        }
    }

    private PaginationInfo buildPaginationInfo(PaginationRequest paginationRequest, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / paginationRequest.getLimit());
        int currentPage = paginationRequest.getOffset() / paginationRequest.getLimit();
        boolean hasNext = (paginationRequest.getOffset() + paginationRequest.getLimit()) < totalElements;
        boolean hasPrevious = paginationRequest.getOffset() > 0;
        
        return new PaginationInfo(
            paginationRequest.getOffset(),
            paginationRequest.getLimit(),
            totalElements,
            totalPages,
            currentPage,
            hasNext,
            hasPrevious
        );
    }

    private class LeadTaskResponseRowMapper implements RowMapper<LeadTaskResponse> {
        private final UUID leadIdentifier;
        
        public LeadTaskResponseRowMapper(UUID leadIdentifier) {
            this.leadIdentifier = leadIdentifier;
        }
        
        @Override
        public LeadTaskResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            Map<String, Object> outcomeDetailsMap = parseJsonColumn(rs, "outcome_details");
            Map<String, Object> taskDetailsMap = parseJsonColumn(rs, "task_details");
            Map<String, Object> leadTaskDetailsMap = parseJsonColumn(rs, "lead_task_details");
            
            LeadTaskResponse.OutcomeDetails outcomeDetails = null;
            if (ValidationUtils.isNonNull(outcomeDetailsMap) && !outcomeDetailsMap.isEmpty()) {
                Object completedAt = outcomeDetailsMap.get("completedAt");
                outcomeDetails = LeadTaskResponse.OutcomeDetails.builder()
                        .remarks((String) outcomeDetailsMap.get("remarks"))
                        .completedAt(ValidationUtils.isNonNull(completedAt) ? getLocalDateTime(completedAt) : null)
                        .completedBy((String) outcomeDetailsMap.get("completedBy"))
                        .rescheduleReasonCodeValueKey((String) outcomeDetailsMap.get("rescheduleReasonCodeValueKey"))
                        .build();
            }
            
            LeadTaskResponse.TaskDetails taskDetails = null;
            if (ValidationUtils.isNonNull(taskDetailsMap) && !taskDetailsMap.isEmpty()) {
                LeadTaskResponse.TaskDetails.PreferredCallWindow preferredCallWindow = null;
                @SuppressWarnings("unchecked")
                Map<String, Object> preferredCallWindowMap = (Map<String, Object>) taskDetailsMap.get("preferredCallWindow");
                if (ValidationUtils.isNonNull(preferredCallWindowMap) && !preferredCallWindowMap.isEmpty()) {
                    try {
                        // Use ObjectMapper to properly convert the map to PreferredCallWindow, handling date parsing
                        preferredCallWindow = objectMapper.convertValue(preferredCallWindowMap, 
                            LeadTaskResponse.TaskDetails.PreferredCallWindow.class);
                    } catch (Exception e) {
                        // Fallback to manual parsing if convertValue fails
                        Object start = preferredCallWindowMap.get("start");
                        Object end = preferredCallWindowMap.get("end");
                        if (ValidationUtils.isNonNull(start) || ValidationUtils.isNonNull(end)) {
                            preferredCallWindow = LeadTaskResponse.TaskDetails.PreferredCallWindow.builder()
                                    .start(ValidationUtils.isNonNull(start) ? getLocalDateTime(start) : null)
                                    .end(ValidationUtils.isNonNull(end) ? getLocalDateTime(end) : null)
                                    .build();
                        }
                    }
                }
                
                Object iterCount = taskDetailsMap.get("iterationCount");
                Integer iterationCount = ValidationUtils.isNonNull(iterCount) ? (iterCount instanceof Integer ? (Integer) iterCount : ((Number) iterCount).intValue()) : null;
                
                taskDetails = LeadTaskResponse.TaskDetails.builder()
                        .preferredCallWindow(preferredCallWindow)
                        .creatorRemarks((String) taskDetailsMap.get("creatorRemarks"))
                        .iterationCount(iterationCount)
                        .build();
            }
            
            String stageKey = null;
            if (ValidationUtils.isNonNull(leadTaskDetailsMap) && !leadTaskDetailsMap.isEmpty()) {
                stageKey = (String) leadTaskDetailsMap.get("stageKey");
            }
            
            // Store outcome key - will be enriched after query completes
            String outcomeKey = rs.getString("outcome");
            
            return LeadTaskResponse.builder()
                    .id(rs.getLong("lead_task_id"))
                    .leadIdentifier(leadIdentifier)
                    .taskIdentifier(rs.getObject("task_identifier", UUID.class))
                    .taskConfigKey(rs.getString("task_config_key"))
                    .taskName(rs.getString("task_name"))
                    .taskDescription(rs.getString("task_description"))
                    .assignedTo(rs.getString("assigned_to"))
                    .dueAt(getLocalDateTime(rs, "due_at"))
                    .outcome(outcomeKey)
                    .outcomeDetails(outcomeDetails)
                    .taskDetails(taskDetails)
                    .stageKey(stageKey)
                    .createdAt(getLocalDateTime(rs, "lead_task_created_at"))
                    .createdBy(rs.getString("lead_task_created_by"))
                    .updatedAt(getLocalDateTime(rs, "lead_task_updated_at"))
                    .updatedBy(rs.getString("lead_task_updated_by"))
                    .build();
        }
    }

    private LocalDateTime getLocalDateTime(ResultSet rs, String columnName) throws SQLException {
        Object value = rs.getObject(columnName);
        return getLocalDateTime(value);
    }
    
    private LocalDateTime getLocalDateTime(Object value) {
        if (!ValidationUtils.isNonNull(value)) {
            return null;
        }
        
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        
        if (value instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) value).toLocalDateTime();
        }
        
        // Handle string values from JSON parsing
        if (value instanceof String) {
            try {
                String dateString = (String) value;
                // Try ISO format first (e.g., "2024-12-31T17:00:00")
                if (dateString.contains("T")) {
                    return LocalDateTime.parse(dateString);
                }
                // Try custom format used by Hypersistence (e.g., "31-12-2024 17:00:00")
                java.time.format.DateTimeFormatter formatter = 
                    java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                return LocalDateTime.parse(dateString, formatter);
            } catch (Exception e) {
                // If parsing fails, try using ObjectMapper to deserialize
                try {
                    return objectMapper.convertValue(value, LocalDateTime.class);
                } catch (Exception ex) {
                    return null;
                }
            }
        }
        
        // Try using ObjectMapper for other types (Long, Integer representing timestamps, etc.)
        try {
            return objectMapper.convertValue(value, LocalDateTime.class);
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, Object> parseJsonColumn(ResultSet rs, String columnName) {
        try {
            String jsonString = rs.getString(columnName);
            if (!ValidationUtils.isNonNullOrEmpty(jsonString)) {
                return null;
            }
            return objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<String> getAvailableAdhocTasks(UUID leadIdentifier, String stageKey) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        
        String workflowConfigKey = getWorkflowConfigKey(lead);
        if (!ValidationUtils.isNonNullOrEmpty(workflowConfigKey)) {
            return new ArrayList<>();
        }
        
        String currentStageKey = getStageKey(lead, stageKey);
        if (!ValidationUtils.isNonNullOrEmpty(currentStageKey)) {
            return new ArrayList<>();
        }
        
        return workflowOrchestratorService.getAdhocTaskKeysForStage(workflowConfigKey, currentStageKey);
    }

    private String getWorkflowConfigKey(Lead lead) {
        Lead.WorkflowDetails workflowDetails = lead.getWorkflowDetails();
        if (ValidationUtils.isNonNull(workflowDetails)) {
            return workflowDetails.getWorkflowConfigKey();
        }
        return null;
    }

    private String getStageKey(Lead lead, String providedStageKey) {
        if (ValidationUtils.isNonNull(providedStageKey)) {
            return providedStageKey;
        }
        
        Lead.WorkflowDetails workflowDetails = lead.getWorkflowDetails();
        if (ValidationUtils.isNonNull(workflowDetails) 
                && ValidationUtils.isNonNull(workflowDetails.getCurrentStageDetails())) {
            String stageKey = workflowDetails.getCurrentStageDetails().getStageKey();
            if (ValidationUtils.isNonNull(stageKey)) {
                return stageKey;
            }
        }
        
        // Fallback to latest stage history
        Optional<LeadStageHistory> latestEntry = leadStageHistoryRepositoryWrapper.findLatestEntry(lead.getId());
        if (latestEntry.isPresent()) {
            return latestEntry.get().getStageKey();
        }
        
        return null;
    }
}

