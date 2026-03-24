package com.nivasafinance.externals.whatsapp.service.impl;

import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppTaskRequest;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppTaskResponse;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppUpdateTaskRequest;
import com.nivasafinance.externals.whatsapp.service.WhatsAppTaskService;
import com.nivasafinance.features.lead.entity.Contact;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.ContactRepositoryWrapper;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.repository.PersonRepositoryWrapper;
import com.nivasafinance.features.task.dto.CreateAdhocTaskRequest;
import com.nivasafinance.features.task.dto.TaskDetailsRequest;
import com.nivasafinance.features.task.dto.TaskResponse;
import com.nivasafinance.features.task.entity.Task;
import com.nivasafinance.features.task.exception.TaskNotFoundException;
import com.nivasafinance.features.task.repository.TaskRepositoryWrapper;
import com.nivasafinance.features.task.service.TaskWriteService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class WhatsAppTaskServiceImpl implements WhatsAppTaskService {

    private static final int DEFAULT_DUE_DATE_HOURS = 24;

    private final TaskWriteService taskWriteService;
    private final TaskRepositoryWrapper taskRepositoryWrapper;
    private final MessageSource messageSource;
    private final PersonRepositoryWrapper personRepositoryWrapper;
    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final ContactRepositoryWrapper contactRepositoryWrapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public WhatsAppTaskResponse createTask(WhatsAppTaskRequest request) {
        UUID leadIdentifier = request.getLeadIdentifier();
        WhatsAppTaskRequest.TaskDetails taskDetails = request.getTaskDetails();
        String taskConfigKey = taskDetails.getTaskConfigKey();

        List<Task> openTasks = taskRepositoryWrapper.findOpenTasksByLeadIdentifier(leadIdentifier);
        Task existingTask = openTasks.stream()
                .filter(task -> taskConfigKey.equals(task.getTaskConfigKey()))
                .findFirst()
                .orElse(null);

        if (existingTask != null) {
            log.info("Task with config key {} already exists for lead {}. Returning existing task.",
                    taskConfigKey, leadIdentifier);
            return WhatsAppTaskResponse.builder()
                    .taskIdentifier(existingTask.getTaskIdentifier())
                    .build();
        }

        LocalDateTime dueAt = taskDetails.getDueAt();
        if (dueAt == null) {
            dueAt = LocalDateTime.now().plusHours(DEFAULT_DUE_DATE_HOURS);
            log.debug("Due date not provided, setting default to 24 hours from now: {}", dueAt);
        }

        String assignedTo = resolveAssignedTo(taskDetails.getAssignedTo(), openTasks);

        TaskDetailsRequest.PreferredCallWindow preferredCallWindow = null;
        if (taskDetails.getPreferredCallWindow() != null) {
            preferredCallWindow = TaskDetailsRequest.PreferredCallWindow.builder()
                    .start(taskDetails.getPreferredCallWindow().getStart())
                    .end(taskDetails.getPreferredCallWindow().getEnd())
                    .build();
        }

        TaskDetailsRequest taskDetailsRequest = TaskDetailsRequest.builder()
                .entityId(leadIdentifier)
                .entityType(EntityType.LEAD)
                .stageKey(taskDetails.getStageKey())
                .creatorRemarks(taskDetails.getCreatorRemarks())
                .preferredCallWindow(preferredCallWindow)
                .build();

        CreateAdhocTaskRequest createAdhocTaskRequest = CreateAdhocTaskRequest.builder()
                .taskConfigKey(taskConfigKey)
                .assignedTo(assignedTo)
                .dueAt(dueAt)
                .taskDetails(taskDetailsRequest)
                .build();

        TaskResponse taskResponse = taskWriteService.createAdhocTask(createAdhocTaskRequest);

        return WhatsAppTaskResponse.builder()
                .taskIdentifier(taskResponse.getTaskIdentifier())
                .build();
    }

    @Override
    @Transactional
    public WhatsAppTaskResponse updateTask(WhatsAppUpdateTaskRequest request) {
        UUID leadIdentifier = resolveLeadIdentifier(request);
        String taskConfigKey = request.getTaskConfigKey();

        List<Task> openTasks = taskRepositoryWrapper.findOpenTasksByLeadIdentifier(leadIdentifier);
        Task task = openTasks.stream()
                .filter(t -> taskConfigKey.equals(t.getTaskConfigKey()))
                .findFirst()
                .orElse(null);

        if (task == null) {
            log.info("Task with config key {} not found for lead {}. Creating new task.", 
                    taskConfigKey, leadIdentifier);
            return createTaskForUpdate(leadIdentifier, request);
        }

        Task.TaskDetails taskDetails = task.getTaskDetails();
        if (taskDetails == null) {
            taskDetails = Task.TaskDetails.builder()
                    .entityId(leadIdentifier)
                    .entityType(EntityType.LEAD)
                    .build();
            task.setTaskDetails(taskDetails);
        }

        if (request.getPreferredStartTime() != null && !request.getPreferredStartTime().isBlank() &&
            request.getPreferredEndTime() != null && !request.getPreferredEndTime().isBlank()) {
            LocalTime startTime = parseTime(request.getPreferredStartTime());
            LocalTime endTime = parseTime(request.getPreferredEndTime());
            LocalDate dateForTime = getDateForPreferredTimeWindow(endTime);

            LocalDateTime preferredStartDateTime = LocalDateTime.of(dateForTime, startTime);
            LocalDateTime preferredEndDateTime = LocalDateTime.of(dateForTime, endTime);

            Task.TaskDetails.PreferredCallWindow preferredCallWindow = Task.TaskDetails.PreferredCallWindow.builder()
                    .start(preferredStartDateTime)
                    .end(preferredEndDateTime)
                    .build();

            taskDetails.setPreferredCallWindow(preferredCallWindow);
        }

        if (request.getCreatorRemarks() != null && !request.getCreatorRemarks().isBlank()) {
            String existingRemarks = taskDetails.getCreatorRemarks();
            if (existingRemarks != null && !existingRemarks.isBlank()) {
                taskDetails.setCreatorRemarks(existingRemarks + "\n" + request.getCreatorRemarks());
            } else {
                taskDetails.setCreatorRemarks(request.getCreatorRemarks());
            }
        }

        Task savedTask = taskRepositoryWrapper.saveWithException(task);

        log.info("Updated task {} for lead {}", savedTask.getTaskIdentifier(), leadIdentifier);

        return WhatsAppTaskResponse.builder()
                .taskIdentifier(savedTask.getTaskIdentifier())
                .build();
    }

    private WhatsAppTaskResponse createTaskForUpdate(UUID leadIdentifier, WhatsAppUpdateTaskRequest request) {
        WhatsAppTaskRequest.TaskDetails taskDetails = new WhatsAppTaskRequest.TaskDetails();
        taskDetails.setTaskConfigKey(request.getTaskConfigKey());
        taskDetails.setCreatorRemarks(request.getCreatorRemarks());

        if (request.getPreferredStartTime() != null && !request.getPreferredStartTime().isBlank() &&
            request.getPreferredEndTime() != null && !request.getPreferredEndTime().isBlank()) {
            LocalTime startTime = parseTime(request.getPreferredStartTime());
            LocalTime endTime = parseTime(request.getPreferredEndTime());
            LocalDate dateForTime = getDateForPreferredTimeWindow(endTime);

            LocalDateTime preferredStartDateTime = LocalDateTime.of(dateForTime, startTime);
            LocalDateTime preferredEndDateTime = LocalDateTime.of(dateForTime, endTime);

            WhatsAppTaskRequest.TaskDetails.PreferredCallWindow preferredCallWindow = 
                    new WhatsAppTaskRequest.TaskDetails.PreferredCallWindow();
            preferredCallWindow.setStart(preferredStartDateTime);
            preferredCallWindow.setEnd(preferredEndDateTime);
            taskDetails.setPreferredCallWindow(preferredCallWindow);
        }

        WhatsAppTaskRequest createRequest = new WhatsAppTaskRequest();
        createRequest.setLeadIdentifier(leadIdentifier);
        createRequest.setTaskDetails(taskDetails);

        return createTask(createRequest);
    }

    private String resolveAssignedTo(String requestAssignedTo, List<Task> openTasks) {
        if (!ValidationUtils.isNullOrEmpty(requestAssignedTo)) {
            return requestAssignedTo;
        }

        return openTasks.stream()
                .map(Task::getAssignedTo)
                .filter(assignedTo -> !ValidationUtils.isNullOrEmpty(assignedTo))
                .findFirst()
                .orElse(null);
    }

    private LocalDate getDateForPreferredTimeWindow(LocalTime preferredEndTime) {
        LocalTime currentTime = LocalTime.now();
        LocalDate today = LocalDate.now();
        
        // If current time is before the preferred end time, use today's date
        // If current time is at or after the preferred end time, use tomorrow's date
        if (currentTime.isBefore(preferredEndTime)) {
            return today;
        } else {
            return today.plusDays(1);
        }
    }

    private LocalTime parseTime(String timeString) {
        if (timeString == null || timeString.isBlank()) {
            throw new IllegalArgumentException("Time string cannot be null or empty");
        }

        String cleanedTime = timeString.trim();
        if (cleanedTime.startsWith("T")) {
            cleanedTime = cleanedTime.substring(1);
        }

        DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("HH:mm:ss.SSS"),
                DateTimeFormatter.ofPattern("HH:mm:ss"),
                DateTimeFormatter.ofPattern("HH:mm")
        };

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalTime.parse(cleanedTime, formatter);
            } catch (DateTimeParseException e) {
                // Try next formatter
            }
        }

        throw new IllegalArgumentException("Invalid time format: " + timeString + 
                ". Expected formats: HH:mm:ss.SSS, HH:mm:ss, or HH:mm (with or without 'T' prefix)");
    }

    private UUID resolveLeadIdentifier(WhatsAppUpdateTaskRequest request) {
        if (ValidationUtils.isNonNull(request.getLeadIdentifier())) {
            return request.getLeadIdentifier();
        }

        if (ValidationUtils.isNullOrEmpty(request.getMobileNumber())) {
            throw new IllegalArgumentException("Either leadIdentifier or mobileNumber (10 digits) must be provided");
        }

        Optional<Person> person = personRepositoryWrapper.findByPrimaryMobileNumber(request.getMobileNumber());
        if (person.isEmpty()) {
            throw new IllegalArgumentException("No person found with mobile number: " + request.getMobileNumber());
        }

        Optional<Lead> lead = findLeadByContactPersonId(person.get().getId());
        if (lead.isEmpty()) {
            throw new IllegalArgumentException("No lead found for mobile number: " + request.getMobileNumber());
        }

        return lead.get().getLeadIdentifier();
    }

    private Optional<Lead> findLeadByContactPersonId(Long personId) {
        Optional<Lead> activeLead = findActiveLeadByContactPersonId(personId);
        if (activeLead.isPresent()) {
            return activeLead;
        }
        return findAnyLeadByContactPersonId(personId);
    }

    private Optional<Lead> findActiveLeadByContactPersonId(Long personId) {
        try {
            String sql = """
                SELECT l.id
                FROM n_lead l
                JOIN LATERAL (
                    SELECT (contact_id)::bigint as id
                    FROM jsonb_array_elements(l.contacts) AS contact_id
                ) contact_ids ON true
                JOIN n_contact c ON c.id = contact_ids.id
                WHERE c.person_id = ?
                  AND l.status = 'ACTIVE'
                ORDER BY l.updated_at DESC
                LIMIT 1
                """;

            Long leadId = jdbcTemplate.queryForObject(sql, Long.class, personId);
            Lead lead = leadId != null ? leadRepositoryWrapper.findByIdWithException(leadId) : null;
            return Optional.ofNullable(lead);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to find active lead by contact person", e);
        }
    }

    private Optional<Lead> findAnyLeadByContactPersonId(Long personId) {
        try {
            String sql = """
                SELECT l.id
                FROM n_lead l
                JOIN LATERAL (
                    SELECT (contact_id)::bigint as id
                    FROM jsonb_array_elements(l.contacts) AS contact_id
                ) contact_ids ON true
                JOIN n_contact c ON c.id = contact_ids.id
                WHERE c.person_id = ?
                ORDER BY l.updated_at DESC
                LIMIT 1
                """;

            Long leadId = jdbcTemplate.queryForObject(sql, Long.class, personId);
            Lead lead = leadId != null ? leadRepositoryWrapper.findByIdWithException(leadId) : null;
            return Optional.ofNullable(lead);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to find lead by contact person", e);
        }
    }
}
