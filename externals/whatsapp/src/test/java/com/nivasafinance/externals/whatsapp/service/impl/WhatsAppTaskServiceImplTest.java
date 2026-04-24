package com.nivasafinance.externals.whatsapp.service.impl;

import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppTaskRequest;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppTaskResponse;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppUpdateTaskRequest;
import com.nivasafinance.features.lead.dto.LeadResponse;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.ContactRepositoryWrapper;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.repository.PersonRepositoryWrapper;
import com.nivasafinance.features.task.dto.CreateAdhocTaskRequest;
import com.nivasafinance.features.task.dto.TaskResponse;
import com.nivasafinance.features.task.entity.Task;
import com.nivasafinance.features.task.repository.TaskRepositoryWrapper;
import com.nivasafinance.features.task.service.TaskWriteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WhatsAppTaskServiceImplTest {

    @Mock
    private TaskWriteService taskWriteService;

    @Mock
    private TaskRepositoryWrapper taskRepositoryWrapper;

    @Mock
    private MessageSource messageSource;

    @Mock
    private PersonRepositoryWrapper personRepositoryWrapper;

    @Mock
    private LeadRepositoryWrapper leadRepositoryWrapper;

    @Mock
    private ContactRepositoryWrapper contactRepositoryWrapper;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private WhatsAppTaskServiceImpl service;

    // ── createTask: existing task ──

    @Test
    void createTask_whenOpenTaskExistsWithSameConfigKey_returnsExistingTask() {
        UUID leadIdentifier = UUID.randomUUID();
        UUID taskIdentifier = UUID.randomUUID();

        Task existingTask = new Task();
        existingTask.setTaskConfigKey("CALLBACK");
        ReflectionTestUtils.setField(existingTask, "taskIdentifier", taskIdentifier);

        when(taskRepositoryWrapper.findOpenTasksByLeadIdentifier(leadIdentifier))
                .thenReturn(List.of(existingTask));

        WhatsAppTaskRequest request = buildCreateTaskRequest(leadIdentifier, "CALLBACK");

        WhatsAppTaskResponse result = service.createTask(request);

        assertEquals(taskIdentifier, result.getTaskIdentifier(),
                "Should return existing task identifier when task with same config key exists");
        verifyNoInteractions(taskWriteService);
    }

    // ── createTask: new task ──

    @Test
    void createTask_whenNoExistingTask_createsNewTask() {
        UUID leadIdentifier = UUID.randomUUID();
        UUID taskIdentifier = UUID.randomUUID();

        when(taskRepositoryWrapper.findOpenTasksByLeadIdentifier(leadIdentifier))
                .thenReturn(List.of());

        TaskResponse taskResponse = TaskResponse.builder()
                .taskIdentifier(taskIdentifier)
                .build();
        when(taskWriteService.createAdhocTask(any(CreateAdhocTaskRequest.class))).thenReturn(taskResponse);

        WhatsAppTaskRequest request = buildCreateTaskRequest(leadIdentifier, "CALLBACK");
        request.getTaskDetails().setDueAt(LocalDateTime.of(2026, 5, 1, 10, 0));
        request.getTaskDetails().setAssignedTo("agent1");
        request.getTaskDetails().setStageKey("VERIFICATION");

        WhatsAppTaskResponse result = service.createTask(request);

        assertEquals(taskIdentifier, result.getTaskIdentifier(),
                "Should return newly created task identifier");

        ArgumentCaptor<CreateAdhocTaskRequest> captor = ArgumentCaptor.forClass(CreateAdhocTaskRequest.class);
        verify(taskWriteService).createAdhocTask(captor.capture());
        assertEquals("CALLBACK", captor.getValue().getTaskConfigKey(),
                "Task config key should match request");
        assertEquals("agent1", captor.getValue().getAssignedTo(),
                "Assigned to should match request");
        assertEquals(LocalDateTime.of(2026, 5, 1, 10, 0), captor.getValue().getDueAt(),
                "Due date should match explicit request value");
    }

    @Test
    void createTask_withPreferredCallWindow_setsWindowInTaskDetails() {
        UUID leadIdentifier = UUID.randomUUID();
        UUID taskIdentifier = UUID.randomUUID();

        when(taskRepositoryWrapper.findOpenTasksByLeadIdentifier(leadIdentifier))
                .thenReturn(List.of());

        TaskResponse taskResponse = TaskResponse.builder().taskIdentifier(taskIdentifier).build();
        when(taskWriteService.createAdhocTask(any(CreateAdhocTaskRequest.class))).thenReturn(taskResponse);

        LocalDateTime windowStart = LocalDateTime.of(2026, 5, 1, 10, 0);
        LocalDateTime windowEnd = LocalDateTime.of(2026, 5, 1, 12, 0);

        WhatsAppTaskRequest request = buildCreateTaskRequest(leadIdentifier, "CALLBACK");
        request.getTaskDetails().setDueAt(LocalDateTime.of(2026, 5, 1, 10, 0));

        WhatsAppTaskRequest.TaskDetails.PreferredCallWindow window =
                new WhatsAppTaskRequest.TaskDetails.PreferredCallWindow();
        window.setStart(windowStart);
        window.setEnd(windowEnd);
        request.getTaskDetails().setPreferredCallWindow(window);

        service.createTask(request);

        ArgumentCaptor<CreateAdhocTaskRequest> captor = ArgumentCaptor.forClass(CreateAdhocTaskRequest.class);
        verify(taskWriteService).createAdhocTask(captor.capture());
        assertNotNull(captor.getValue().getTaskDetails().getPreferredCallWindow(),
                "Preferred call window should be set in task details");
        assertEquals(windowStart, captor.getValue().getTaskDetails().getPreferredCallWindow().getStart(),
                "Call window start should match request");
        assertEquals(windowEnd, captor.getValue().getTaskDetails().getPreferredCallWindow().getEnd(),
                "Call window end should match request");
    }

    // ── createTask: dueAt resolution ──

    @Test
    void createTask_whenDueAtNotProvided_passesNullDueAtToTaskModule() {
        UUID leadIdentifier = UUID.randomUUID();
        UUID taskIdentifier = UUID.randomUUID();

        when(taskRepositoryWrapper.findOpenTasksByLeadIdentifier(leadIdentifier))
                .thenReturn(List.of());

        TaskResponse taskResponse = TaskResponse.builder().taskIdentifier(taskIdentifier).build();
        when(taskWriteService.createAdhocTask(any(CreateAdhocTaskRequest.class))).thenReturn(taskResponse);

        WhatsAppTaskRequest request = buildCreateTaskRequest(leadIdentifier, "CALLBACK");

        service.createTask(request);

        ArgumentCaptor<CreateAdhocTaskRequest> captor = ArgumentCaptor.forClass(CreateAdhocTaskRequest.class);
        verify(taskWriteService).createAdhocTask(captor.capture());
        assertNull(captor.getValue().getDueAt(),
                "Omitted dueAt should be null so the task module applies task-config due date logic");
    }

    // ── createTask: assignedTo resolution ──

    @Test
    void createTask_whenAssignedToNotProvided_usesLeadStageOwner() {
        UUID leadIdentifier = UUID.randomUUID();
        UUID taskIdentifier = UUID.randomUUID();

        when(taskRepositoryWrapper.findOpenTasksByLeadIdentifier(leadIdentifier))
                .thenReturn(List.of());

        LeadResponse leadResponse = LeadResponse.builder()
                .assignedTo("stageOwner")
                .build();
        when(leadRepositoryWrapper.findLeadResponseByIdentifierWithException(leadIdentifier))
                .thenReturn(leadResponse);

        TaskResponse taskResponse = TaskResponse.builder().taskIdentifier(taskIdentifier).build();
        when(taskWriteService.createAdhocTask(any(CreateAdhocTaskRequest.class))).thenReturn(taskResponse);

        WhatsAppTaskRequest request = buildCreateTaskRequest(leadIdentifier, "CALLBACK");
        request.getTaskDetails().setDueAt(LocalDateTime.of(2026, 5, 1, 10, 0));

        service.createTask(request);

        ArgumentCaptor<CreateAdhocTaskRequest> captor = ArgumentCaptor.forClass(CreateAdhocTaskRequest.class);
        verify(taskWriteService).createAdhocTask(captor.capture());
        assertEquals("stageOwner", captor.getValue().getAssignedTo(),
                "Should use lead stage owner when assignedTo is not provided in request");
    }

    // ── updateTask: existing task ──

    @Test
    void updateTask_whenTaskExists_updatesPreferredCallWindow() {
        UUID leadIdentifier = UUID.randomUUID();
        UUID taskIdentifier = UUID.randomUUID();

        Task task = new Task();
        ReflectionTestUtils.setField(task, "taskIdentifier", taskIdentifier);
        task.setTaskConfigKey("CALLBACK");
        task.setTaskDetails(Task.TaskDetails.builder()
                .entityId(leadIdentifier)
                .entityType(EntityType.LEAD)
                .build());

        when(taskRepositoryWrapper.findOpenTasksByLeadIdentifier(leadIdentifier))
                .thenReturn(List.of(task));
        when(taskRepositoryWrapper.saveWithException(any(Task.class))).thenReturn(task);

        WhatsAppUpdateTaskRequest request = new WhatsAppUpdateTaskRequest();
        request.setLeadIdentifier(leadIdentifier);
        request.setTaskConfigKey("CALLBACK");
        request.setPreferredStartTime("10:00");
        request.setPreferredEndTime("23:59");

        WhatsAppTaskResponse result = service.updateTask(request);

        assertEquals(taskIdentifier, result.getTaskIdentifier(),
                "Should return updated task identifier");
        verify(taskRepositoryWrapper).saveWithException(any(Task.class));
    }

    @Test
    void updateTask_whenTaskExistsAndHasCreatorRemarks_appendsNewRemarks() {
        UUID leadIdentifier = UUID.randomUUID();
        UUID taskIdentifier = UUID.randomUUID();

        Task task = new Task();
        ReflectionTestUtils.setField(task, "taskIdentifier", taskIdentifier);
        task.setTaskConfigKey("CALLBACK");
        task.setTaskDetails(Task.TaskDetails.builder()
                .entityId(leadIdentifier)
                .entityType(EntityType.LEAD)
                .creatorRemarks("Existing remarks")
                .build());

        when(taskRepositoryWrapper.findOpenTasksByLeadIdentifier(leadIdentifier))
                .thenReturn(List.of(task));
        when(taskRepositoryWrapper.saveWithException(any(Task.class))).thenReturn(task);

        WhatsAppUpdateTaskRequest request = new WhatsAppUpdateTaskRequest();
        request.setLeadIdentifier(leadIdentifier);
        request.setTaskConfigKey("CALLBACK");
        request.setCreatorRemarks("New remarks");

        service.updateTask(request);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepositoryWrapper).saveWithException(captor.capture());
        assertEquals("Existing remarks\nNew remarks", captor.getValue().getTaskDetails().getCreatorRemarks(),
                "New remarks should be appended to existing remarks with newline separator");
    }

    @Test
    void updateTask_whenTaskExistsAndHasNoExistingRemarks_setsNewRemarks() {
        UUID leadIdentifier = UUID.randomUUID();
        UUID taskIdentifier = UUID.randomUUID();

        Task task = new Task();
        ReflectionTestUtils.setField(task, "taskIdentifier", taskIdentifier);
        task.setTaskConfigKey("CALLBACK");
        task.setTaskDetails(Task.TaskDetails.builder()
                .entityId(leadIdentifier)
                .entityType(EntityType.LEAD)
                .build());

        when(taskRepositoryWrapper.findOpenTasksByLeadIdentifier(leadIdentifier))
                .thenReturn(List.of(task));
        when(taskRepositoryWrapper.saveWithException(any(Task.class))).thenReturn(task);

        WhatsAppUpdateTaskRequest request = new WhatsAppUpdateTaskRequest();
        request.setLeadIdentifier(leadIdentifier);
        request.setTaskConfigKey("CALLBACK");
        request.setCreatorRemarks("New remarks");

        service.updateTask(request);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepositoryWrapper).saveWithException(captor.capture());
        assertEquals("New remarks", captor.getValue().getTaskDetails().getCreatorRemarks(),
                "Should set creator remarks directly when no existing remarks");
    }

    @Test
    void updateTask_whenTaskExistsWithNullTaskDetails_initializesTaskDetails() {
        UUID leadIdentifier = UUID.randomUUID();
        UUID taskIdentifier = UUID.randomUUID();

        Task task = new Task();
        ReflectionTestUtils.setField(task, "taskIdentifier", taskIdentifier);
        task.setTaskConfigKey("CALLBACK");
        task.setTaskDetails(null);

        when(taskRepositoryWrapper.findOpenTasksByLeadIdentifier(leadIdentifier))
                .thenReturn(List.of(task));
        when(taskRepositoryWrapper.saveWithException(any(Task.class))).thenReturn(task);

        WhatsAppUpdateTaskRequest request = new WhatsAppUpdateTaskRequest();
        request.setLeadIdentifier(leadIdentifier);
        request.setTaskConfigKey("CALLBACK");
        request.setCreatorRemarks("Remarks");

        service.updateTask(request);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepositoryWrapper).saveWithException(captor.capture());
        assertNotNull(captor.getValue().getTaskDetails(),
                "Task details should be initialized when null");
        assertEquals("Remarks", captor.getValue().getTaskDetails().getCreatorRemarks(),
                "Creator remarks should be set on the newly initialized task details");
    }

    // ── updateTask: task not found → creates new ──

    @Test
    void updateTask_whenTaskNotFound_createsNewTask() {
        UUID leadIdentifier = UUID.randomUUID();
        UUID taskIdentifier = UUID.randomUUID();

        when(taskRepositoryWrapper.findOpenTasksByLeadIdentifier(leadIdentifier))
                .thenReturn(List.of());

        TaskResponse taskResponse = TaskResponse.builder().taskIdentifier(taskIdentifier).build();
        when(taskWriteService.createAdhocTask(any(CreateAdhocTaskRequest.class))).thenReturn(taskResponse);

        WhatsAppUpdateTaskRequest request = new WhatsAppUpdateTaskRequest();
        request.setLeadIdentifier(leadIdentifier);
        request.setTaskConfigKey("CALLBACK");
        request.setCreatorRemarks("New task remarks");

        WhatsAppTaskResponse result = service.updateTask(request);

        assertEquals(taskIdentifier, result.getTaskIdentifier(),
                "Should create and return a new task when no existing task found");
        verify(taskWriteService).createAdhocTask(any(CreateAdhocTaskRequest.class));
    }

    // ── updateTask: resolveLeadIdentifier ──

    @Test
    void updateTask_withLeadIdentifier_usesProvidedIdentifier() {
        UUID leadIdentifier = UUID.randomUUID();
        UUID taskIdentifier = UUID.randomUUID();

        Task task = new Task();
        ReflectionTestUtils.setField(task, "taskIdentifier", taskIdentifier);
        task.setTaskConfigKey("CALLBACK");
        task.setTaskDetails(Task.TaskDetails.builder().build());

        when(taskRepositoryWrapper.findOpenTasksByLeadIdentifier(leadIdentifier))
                .thenReturn(List.of(task));
        when(taskRepositoryWrapper.saveWithException(any(Task.class))).thenReturn(task);

        WhatsAppUpdateTaskRequest request = new WhatsAppUpdateTaskRequest();
        request.setLeadIdentifier(leadIdentifier);
        request.setTaskConfigKey("CALLBACK");

        service.updateTask(request);

        verify(taskRepositoryWrapper).findOpenTasksByLeadIdentifier(leadIdentifier);
        verifyNoInteractions(personRepositoryWrapper, jdbcTemplate);
    }

    @Test
    void createTask_withMobileNumber_resolvesLeadIdentifier() {
        Long personId = 1L;
        Long leadId = 10L;
        UUID leadIdentifier = UUID.randomUUID();
        UUID taskIdentifier = UUID.randomUUID();

        Person person = new Person();
        person.setId(personId);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210"))
                .thenReturn(Optional.of(person));

        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(personId)))
                .thenReturn(leadId);

        Lead lead = new Lead();
        lead.setLeadIdentifier(leadIdentifier);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);

        when(taskRepositoryWrapper.findOpenTasksByLeadIdentifier(leadIdentifier))
                .thenReturn(List.of());

        TaskResponse taskResponse = TaskResponse.builder().taskIdentifier(taskIdentifier).build();
        when(taskWriteService.createAdhocTask(any(CreateAdhocTaskRequest.class))).thenReturn(taskResponse);

        WhatsAppTaskRequest request = new WhatsAppTaskRequest();
        request.setMobileNumber("9876543210");
        WhatsAppTaskRequest.TaskDetails taskDetails = new WhatsAppTaskRequest.TaskDetails();
        taskDetails.setTaskConfigKey("CALLBACK");
        request.setTaskDetails(taskDetails);

        WhatsAppTaskResponse result = service.createTask(request);

        assertEquals(taskIdentifier, result.getTaskIdentifier(),
                "Should resolve lead via mobile number and create task");
        verify(taskRepositoryWrapper).findOpenTasksByLeadIdentifier(leadIdentifier);
        verify(taskWriteService).createAdhocTask(any(CreateAdhocTaskRequest.class));
    }

    @Test
    void createTask_withNullLeadIdentifierAndNullMobileNumber_throwsIllegalArgumentException() {
        WhatsAppTaskRequest request = new WhatsAppTaskRequest();
        WhatsAppTaskRequest.TaskDetails taskDetails = new WhatsAppTaskRequest.TaskDetails();
        taskDetails.setTaskConfigKey("CALLBACK");
        request.setTaskDetails(taskDetails);

        assertThrows(IllegalArgumentException.class,
                () -> service.createTask(request),
                "Should throw when neither leadIdentifier nor mobileNumber is provided");
    }

    @Test
    void updateTask_withMobileNumber_resolvesLeadIdentifier() {
        Long personId = 1L;
        Long leadId = 10L;
        UUID leadIdentifier = UUID.randomUUID();
        UUID taskIdentifier = UUID.randomUUID();

        Person person = new Person();
        person.setId(personId);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210"))
                .thenReturn(Optional.of(person));

        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(personId)))
                .thenReturn(leadId);

        Lead lead = new Lead();
        lead.setLeadIdentifier(leadIdentifier);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);

        Task task = new Task();
        ReflectionTestUtils.setField(task, "taskIdentifier", taskIdentifier);
        task.setTaskConfigKey("CALLBACK");
        task.setTaskDetails(Task.TaskDetails.builder().build());

        when(taskRepositoryWrapper.findOpenTasksByLeadIdentifier(leadIdentifier))
                .thenReturn(List.of(task));
        when(taskRepositoryWrapper.saveWithException(any(Task.class))).thenReturn(task);

        WhatsAppUpdateTaskRequest request = new WhatsAppUpdateTaskRequest();
        request.setMobileNumber("9876543210");
        request.setTaskConfigKey("CALLBACK");

        WhatsAppTaskResponse result = service.updateTask(request);

        assertEquals(taskIdentifier, result.getTaskIdentifier(),
                "Should resolve lead via mobile number and return task identifier");
    }

    @Test
    void updateTask_withNullLeadIdentifierAndNullMobileNumber_throwsIllegalArgumentException() {
        WhatsAppUpdateTaskRequest request = new WhatsAppUpdateTaskRequest();
        request.setTaskConfigKey("CALLBACK");

        assertThrows(IllegalArgumentException.class,
                () -> service.updateTask(request),
                "Should throw when neither leadIdentifier nor mobileNumber is provided");
    }

    @Test
    void updateTask_whenPersonNotFoundByMobileNumber_throwsIllegalArgumentException() {
        WhatsAppUpdateTaskRequest request = new WhatsAppUpdateTaskRequest();
        request.setMobileNumber("9876543210");
        request.setTaskConfigKey("CALLBACK");

        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210"))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.updateTask(request),
                "Should throw when no person found for mobile number");
        assertTrue(ex.getMessage().contains("9876543210"),
                "Exception message should contain the mobile number");
    }

    @Test
    void updateTask_whenNoLeadFoundForPerson_throwsIllegalArgumentException() {
        Long personId = 1L;

        WhatsAppUpdateTaskRequest request = new WhatsAppUpdateTaskRequest();
        request.setMobileNumber("9876543210");
        request.setTaskConfigKey("CALLBACK");

        Person person = new Person();
        person.setId(personId);
        when(personRepositoryWrapper.findByPrimaryMobileNumber("9876543210"))
                .thenReturn(Optional.of(person));

        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(personId)))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThrows(IllegalArgumentException.class,
                () -> service.updateTask(request),
                "Should throw when no lead found for person");
    }

    // ── helpers ──

    private WhatsAppTaskRequest buildCreateTaskRequest(UUID leadIdentifier, String taskConfigKey) {
        WhatsAppTaskRequest.TaskDetails taskDetails = new WhatsAppTaskRequest.TaskDetails();
        taskDetails.setTaskConfigKey(taskConfigKey);

        WhatsAppTaskRequest request = new WhatsAppTaskRequest();
        request.setLeadIdentifier(leadIdentifier);
        request.setTaskDetails(taskDetails);
        return request;
    }
}
