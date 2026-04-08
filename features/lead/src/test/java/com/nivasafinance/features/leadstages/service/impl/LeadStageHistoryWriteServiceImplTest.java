package com.nivasafinance.features.leadstages.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.enums.EntityType;
import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.leadstages.dto.BulkChangeAssignmentRequest;
import com.nivasafinance.features.leadstages.dto.BulkChangeAssignmentResponse;
import com.nivasafinance.features.leadstages.dto.StageTransitionRequest;
import com.nivasafinance.features.leadstages.entity.LeadStageAssignmentHistory;
import com.nivasafinance.features.leadstages.entity.LeadStageHistory;
import com.nivasafinance.features.leadstages.exception.LeadStageHistoryValidationException;
import com.nivasafinance.features.leadstages.exception.LeadStageValidationException;
import com.nivasafinance.features.leadstages.repository.LeadStageHistoryRepositoryWrapper;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.rolemanagement.enums.Role;
import com.nivasafinance.features.rolemanagement.role.service.UserRoleService;
import com.nivasafinance.features.workflow.dto.PossibleNextStage;
import com.nivasafinance.features.workflow.dto.StageConfigResponse;
import com.nivasafinance.features.workflow.entity.WorkflowConfig;
import com.nivasafinance.features.workflow.exception.WorkflowConfigValidationException;
import com.nivasafinance.features.workflow.orchestrator.WorkflowOrchestratorService;
import com.nivasafinance.features.workflow.service.WorkflowConfigReadService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadStageHistoryWriteServiceImplTest {

    @Mock
    private LeadStageHistoryRepositoryWrapper leadStageHistoryRepositoryWrapper;

    @Mock
    private LeadRepositoryWrapper leadRepositoryWrapper;

    @Mock
    private WorkflowOrchestratorService workflowOrchestratorService;

    @Mock
    private WorkflowConfigReadService workflowConfigReadService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private UserRoleService userRoleService;

    @InjectMocks
    private LeadStageHistoryWriteServiceImpl leadStageHistoryWriteService;

    private UUID leadIdentifier;
    private Long leadId;
    private Lead lead;

    @BeforeEach
    void setUp() {
        UserContext.setUsername("test-user");
        leadIdentifier = UUID.randomUUID();
        leadId = 1L;

        lead = new Lead();
        lead.setId(leadId);
        lead.setLeadIdentifier(leadIdentifier);
    }

    @AfterEach
    void tearDown() {
        UserContext.setUsername(null);
    }

    // ==================== createInitialStage() Tests ====================

    @Test
    void createInitialStage_withNullLeadId_throwsLeadStageValidationException() {
        assertThrows(LeadStageValidationException.class,
                () -> leadStageHistoryWriteService.createInitialStage(null, "wf-key"),
                "Should throw when leadId is null");
        verifyNoInteractions(leadRepositoryWrapper);
    }

    @Test
    void createInitialStage_withNullWorkflowConfigKey_throwsException() {
        assertThrows(Exception.class,
                () -> leadStageHistoryWriteService.createInitialStage(leadIdentifier, null),
                "Should throw when workflowConfigKey is null");
    }

    @Test
    void createInitialStage_withEmptyWorkflowConfigKey_throwsException() {
        assertThrows(Exception.class,
                () -> leadStageHistoryWriteService.createInitialStage(leadIdentifier, ""),
                "Should throw when workflowConfigKey is empty");
    }

    @Test
    void createInitialStage_whenLandingStageNotConfigured_throwsWorkflowConfigValidationException() {
        // Arrange
        WorkflowConfig workflowConfig = new WorkflowConfig();
        workflowConfig.setWorkflowConfigDetails(
                WorkflowConfig.WorkflowConfigDetails.builder().landingStage(null).build());

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(workflowConfigReadService.getWorkflowConfigByKey("wf-key")).thenReturn(workflowConfig);

        // Act & Assert
        assertThrows(WorkflowConfigValidationException.class,
                () -> leadStageHistoryWriteService.createInitialStage(leadIdentifier, "wf-key"),
                "Should throw when landing stage is not configured in workflow");
    }

    @Test
    void createInitialStage_whenConfigDetailsNull_throwsWorkflowConfigValidationException() {
        // Arrange
        WorkflowConfig workflowConfig = new WorkflowConfig();
        workflowConfig.setWorkflowConfigDetails(null);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(workflowConfigReadService.getWorkflowConfigByKey("wf-key")).thenReturn(workflowConfig);

        // Act & Assert
        assertThrows(WorkflowConfigValidationException.class,
                () -> leadStageHistoryWriteService.createInitialStage(leadIdentifier, "wf-key"),
                "Should throw when workflowConfigDetails is null");
    }

    @Test
    void createInitialStage_withNullWorkflowDetails_initializesAndCreatesStage() {
        // Arrange — lead has no workflow details yet
        lead.setWorkflowDetails(null);

        WorkflowConfig workflowConfig = new WorkflowConfig();
        workflowConfig.setWorkflowConfigDetails(
                WorkflowConfig.WorkflowConfigDetails.builder().landingStage("LANDING").build());

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(workflowConfigReadService.getWorkflowConfigByKey("wf-key")).thenReturn(workflowConfig);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // createStageEntry internals
        when(leadStageHistoryRepositoryWrapper.findLatestEntry(leadId)).thenReturn(Optional.empty());
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        doNothing().when(workflowOrchestratorService).validateStageTransition(
                eq(leadId), eq(EntityType.LEAD), eq("LANDING"), any(), eq(false));
        when(leadStageHistoryRepositoryWrapper.save(any(LeadStageHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        LeadStageHistory result = leadStageHistoryWriteService.createInitialStage(leadIdentifier, "wf-key");

        // Assert
        assertNotNull(result, "Should return created stage history");
        assertNotNull(lead.getWorkflowDetails(), "Workflow details should be initialized");
        assertEquals("wf-key", lead.getWorkflowDetails().getWorkflowConfigKey(),
                "Workflow config key should be set on lead");
    }

    @Test
    void createInitialStage_withExistingCurrentStageDetails_usesAssignedToFromLead() {
        // Arrange — lead has workflow details with currentStageDetails.assignedTo
        Lead.CurrentStageDetails csd = Lead.CurrentStageDetails.builder()
                .stageKey("OLD_STAGE").assignedTo("pre-assigned-user").enteredAt(LocalDateTime.now()).build();
        Lead.LastStageDetails lsd = Lead.LastStageDetails.builder()
                .remarks("previous remarks").build();
        Lead.WorkflowDetails wd = Lead.WorkflowDetails.builder()
                .currentStageDetails(csd).lastStageDetails(lsd).build();
        lead.setWorkflowDetails(wd);

        WorkflowConfig workflowConfig = new WorkflowConfig();
        workflowConfig.setWorkflowConfigDetails(
                WorkflowConfig.WorkflowConfigDetails.builder().landingStage("LANDING").build());

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(workflowConfigReadService.getWorkflowConfigByKey("wf-key")).thenReturn(workflowConfig);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // createStageEntry internals
        when(leadStageHistoryRepositoryWrapper.findLatestEntry(leadId)).thenReturn(Optional.empty());
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        doNothing().when(workflowOrchestratorService).validateStageTransition(
                eq(leadId), eq(EntityType.LEAD), eq("LANDING"), eq("pre-assigned-user"), eq(false));
        when(leadStageHistoryRepositoryWrapper.save(any(LeadStageHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        LeadStageHistory result = leadStageHistoryWriteService.createInitialStage(leadIdentifier, "wf-key");

        // Assert
        assertNotNull(result, "Should return created stage history");
        assertEquals("LANDING", result.getStageKey(), "Stage key should be the landing stage");
    }

    // ==================== createStageEntry() Tests ====================

    @Test
    void createStageEntry_withNullLeadId_throwsLeadStageValidationException() {
        StageTransitionRequest request = StageTransitionRequest.builder()
                .stageKey("STAGE_1").build();

        assertThrows(LeadStageValidationException.class,
                () -> leadStageHistoryWriteService.createStageEntry(null, request),
                "Should throw when leadId is null");
        verifyNoInteractions(leadRepositoryWrapper);
    }

    @Test
    void createStageEntry_withNullRequest_throwsLeadStageValidationException() {
        assertThrows(LeadStageValidationException.class,
                () -> leadStageHistoryWriteService.createStageEntry(leadIdentifier, null),
                "Should throw when request is null");
    }

    @Test
    void createStageEntry_withNullStageKey_throwsLeadStageValidationException() {
        StageTransitionRequest request = StageTransitionRequest.builder()
                .stageKey(null).build();

        assertThrows(LeadStageValidationException.class,
                () -> leadStageHistoryWriteService.createStageEntry(leadIdentifier, request),
                "Should throw when stageKey is null");
    }

    @Test
    void createStageEntry_withEmptyStageKey_throwsLeadStageValidationException() {
        StageTransitionRequest request = StageTransitionRequest.builder()
                .stageKey("").build();

        assertThrows(LeadStageValidationException.class,
                () -> leadStageHistoryWriteService.createStageEntry(leadIdentifier, request),
                "Should throw when stageKey is empty");
    }

    @Test
    void createStageEntry_firstEntry_createsHistoryWithoutPreviousStage() {
        // Arrange
        Lead.WorkflowDetails wd = Lead.WorkflowDetails.builder().workflowConfigKey("wf-key").build();
        lead.setWorkflowDetails(wd);

        StageTransitionRequest request = StageTransitionRequest.builder()
                .stageKey("STAGE_1").assignedTo("user1").remarks("initial").build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadStageHistoryRepositoryWrapper.findLatestEntry(leadId)).thenReturn(Optional.empty());
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        doNothing().when(workflowOrchestratorService).validateStageTransition(
                eq(leadId), eq(EntityType.LEAD), eq("STAGE_1"), eq("user1"), eq(false));
        when(workflowOrchestratorService.getDefaultSubStageForStage("wf-key", "STAGE_1")).thenReturn(null);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);
        when(leadStageHistoryRepositoryWrapper.save(any(LeadStageHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        LeadStageHistory result = leadStageHistoryWriteService.createStageEntry(leadIdentifier, request);

        // Assert
        assertNotNull(result, "Should return created stage history for first entry");
        assertEquals("STAGE_1", result.getStageKey(), "Stage key should match request");
        assertNull(result.getStageFrom(), "First entry should have no stageFrom");
        verify(eventPublisher).publishEvent(any(SystemEvent.class));
    }

    @Test
    void createStageEntry_subsequentEntry_closesPreviousAndCreatesNew() {
        // Arrange
        Lead.WorkflowDetails wd = Lead.WorkflowDetails.builder().workflowConfigKey("wf-key").build();
        lead.setWorkflowDetails(wd);

        LeadStageHistory previousEntry = LeadStageHistory.builder()
                .leadId(leadId).stageKey("STAGE_1").enteredAt(LocalDateTime.now())
                .assignmentHistory(new ArrayList<>()).build();

        StageTransitionRequest request = StageTransitionRequest.builder()
                .stageKey("STAGE_2").assignedTo("user2").remarks("moving forward").build();

        PossibleNextStage nextStage = PossibleNextStage.builder()
                .stageKey("STAGE_2").allowedRoles(null).build();
        StageConfigResponse stageConfig = StageConfigResponse.builder()
                .key("STAGE_1").possibleNextStages(List.of(nextStage)).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        // buildCreateLeadStageHistoryRequest calls findLatestEntry once,
        // then validateCreateStageEntryRequest calls findLatestEntry,
        // then createStageEntryInternal calls findLatestEntry again
        when(leadStageHistoryRepositoryWrapper.findLatestEntry(leadId))
                .thenReturn(Optional.of(previousEntry));
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        doNothing().when(workflowOrchestratorService).validateStageTransition(
                eq(leadId), eq(EntityType.LEAD), eq("STAGE_2"), eq("user2"), eq(true));
        when(workflowOrchestratorService.getStageConfig("STAGE_1")).thenReturn(stageConfig);
        when(workflowOrchestratorService.getDefaultSubStageForStage("wf-key", "STAGE_2")).thenReturn(null);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);
        when(leadStageHistoryRepositoryWrapper.save(any(LeadStageHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        LeadStageHistory result = leadStageHistoryWriteService.createStageEntry(leadIdentifier, request);

        // Assert
        assertNotNull(result, "Should return created stage history for subsequent entry");
        assertEquals("STAGE_2", result.getStageKey(), "Stage key should match the new stage");
        assertNotNull(previousEntry.getExitedAt(), "Previous entry should have been closed with exitedAt");
        verify(eventPublisher).publishEvent(any(SystemEvent.class));
    }

    @Test
    void createStageEntry_withNullAssignedTo_skipsAssignmentHistoryCreation() {
        // Arrange — no assignedTo means no LeadStageAssignmentHistory should be created
        Lead.WorkflowDetails wd = Lead.WorkflowDetails.builder().workflowConfigKey("wf-key").build();
        lead.setWorkflowDetails(wd);

        StageTransitionRequest request = StageTransitionRequest.builder()
                .stageKey("STAGE_1").assignedTo(null).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadStageHistoryRepositoryWrapper.findLatestEntry(leadId)).thenReturn(Optional.empty());
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        doNothing().when(workflowOrchestratorService).validateStageTransition(
                eq(leadId), eq(EntityType.LEAD), eq("STAGE_1"), any(), eq(false));
        when(workflowOrchestratorService.getDefaultSubStageForStage("wf-key", "STAGE_1")).thenReturn(null);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);
        when(leadStageHistoryRepositoryWrapper.save(any(LeadStageHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        LeadStageHistory result = leadStageHistoryWriteService.createStageEntry(leadIdentifier, request);

        // Assert
        assertNotNull(result, "Should return created stage history");
        // save called once for the history, not twice (no assignment history save)
        verify(leadStageHistoryRepositoryWrapper, times(1)).save(any(LeadStageHistory.class));
    }

    @Test
    void createStageEntry_withNullUsername_usesSystemAsMovedBy() {
        // Arrange
        UserContext.setUsername(null);
        Lead.WorkflowDetails wd = Lead.WorkflowDetails.builder().workflowConfigKey("wf-key").build();
        lead.setWorkflowDetails(wd);

        StageTransitionRequest request = StageTransitionRequest.builder()
                .stageKey("STAGE_1").build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadStageHistoryRepositoryWrapper.findLatestEntry(leadId)).thenReturn(Optional.empty());
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        doNothing().when(workflowOrchestratorService).validateStageTransition(
                eq(leadId), eq(EntityType.LEAD), eq("STAGE_1"), any(), eq(false));
        when(workflowOrchestratorService.getDefaultSubStageForStage("wf-key", "STAGE_1")).thenReturn(null);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);
        when(leadStageHistoryRepositoryWrapper.save(any(LeadStageHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        LeadStageHistory result = leadStageHistoryWriteService.createStageEntry(leadIdentifier, request);

        // Assert
        assertEquals("system", result.getMovedBy(), "Should use 'system' when UserContext username is null");
    }

    @Test
    void createStageEntry_previousStageKeyProvidedForFirstEntry_throwsException() {
        // Arrange — no history exists but request has previousStageKey set
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadStageHistoryRepositoryWrapper.findLatestEntry(leadId)).thenReturn(Optional.empty());
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        doNothing().when(workflowOrchestratorService).validateStageTransition(
                eq(leadId), eq(EntityType.LEAD), eq("STAGE_2"), any(), eq(false));

        StageTransitionRequest request = StageTransitionRequest.builder()
                .stageKey("STAGE_2").previousStageKey("STAGE_1").build();

        // Act & Assert
        assertThrows(LeadStageHistoryValidationException.class,
                () -> leadStageHistoryWriteService.createStageEntry(leadIdentifier, request),
                "Should throw when previousStageKey is provided but no history exists");
    }

    @Test
    void createStageEntry_invalidStageTransition_throwsException() {
        // Arrange — transition from STAGE_1 to STAGE_3 is not in possibleNextStages
        Lead.WorkflowDetails wd = Lead.WorkflowDetails.builder().workflowConfigKey("wf-key").build();
        lead.setWorkflowDetails(wd);

        LeadStageHistory previousEntry = LeadStageHistory.builder()
                .leadId(leadId).stageKey("STAGE_1").enteredAt(LocalDateTime.now())
                .assignmentHistory(new ArrayList<>()).build();

        PossibleNextStage nextStage = PossibleNextStage.builder()
                .stageKey("STAGE_2").allowedRoles(null).build();
        StageConfigResponse stageConfig = StageConfigResponse.builder()
                .key("STAGE_1").possibleNextStages(List.of(nextStage)).build();

        StageTransitionRequest request = StageTransitionRequest.builder()
                .stageKey("STAGE_3").build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadStageHistoryRepositoryWrapper.findLatestEntry(leadId))
                .thenReturn(Optional.of(previousEntry));
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        doNothing().when(workflowOrchestratorService).validateStageTransition(
                eq(leadId), eq(EntityType.LEAD), eq("STAGE_3"), any(), eq(true));
        when(workflowOrchestratorService.getStageConfig("STAGE_1")).thenReturn(stageConfig);

        // Act & Assert
        assertThrows(LeadStageHistoryValidationException.class,
                () -> leadStageHistoryWriteService.createStageEntry(leadIdentifier, request),
                "Should throw when stage transition is not allowed");
    }

    @Test
    void createStageEntry_roleNotAllowedForTransition_throwsException() {
        // Arrange
        Lead.WorkflowDetails wd = Lead.WorkflowDetails.builder().workflowConfigKey("wf-key").build();
        lead.setWorkflowDetails(wd);

        LeadStageHistory previousEntry = LeadStageHistory.builder()
                .leadId(leadId).stageKey("STAGE_1").enteredAt(LocalDateTime.now())
                .assignmentHistory(new ArrayList<>()).build();

        PossibleNextStage nextStage = PossibleNextStage.builder()
                .stageKey("STAGE_2").allowedRoles(List.of("MANAGER")).build();
        StageConfigResponse stageConfig = StageConfigResponse.builder()
                .key("STAGE_1").possibleNextStages(List.of(nextStage)).build();

        StageTransitionRequest request = StageTransitionRequest.builder()
                .stageKey("STAGE_2").build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadStageHistoryRepositoryWrapper.findLatestEntry(leadId))
                .thenReturn(Optional.of(previousEntry));
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        doNothing().when(workflowOrchestratorService).validateStageTransition(
                eq(leadId), eq(EntityType.LEAD), eq("STAGE_2"), any(), eq(true));
        when(workflowOrchestratorService.getStageConfig("STAGE_1")).thenReturn(stageConfig);
        when(userRoleService.getRolesByUsername("test-user")).thenReturn(List.of("AGENT"));

        // Act & Assert
        assertThrows(LeadStageHistoryValidationException.class,
                () -> leadStageHistoryWriteService.createStageEntry(leadIdentifier, request),
                "Should throw when user role is not allowed for the stage transition");
    }

    @Test
    void createStageEntry_adminRoleBypasses_roleRestriction() {
        // Arrange
        Lead.WorkflowDetails wd = Lead.WorkflowDetails.builder().workflowConfigKey("wf-key").build();
        lead.setWorkflowDetails(wd);

        LeadStageHistory previousEntry = LeadStageHistory.builder()
                .leadId(leadId).stageKey("STAGE_1").enteredAt(LocalDateTime.now())
                .assignmentHistory(new ArrayList<>()).build();

        PossibleNextStage nextStage = PossibleNextStage.builder()
                .stageKey("STAGE_2").allowedRoles(List.of("MANAGER")).build();
        StageConfigResponse stageConfig = StageConfigResponse.builder()
                .key("STAGE_1").possibleNextStages(List.of(nextStage)).build();

        StageTransitionRequest request = StageTransitionRequest.builder()
                .stageKey("STAGE_2").build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadStageHistoryRepositoryWrapper.findLatestEntry(leadId))
                .thenReturn(Optional.of(previousEntry));
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        doNothing().when(workflowOrchestratorService).validateStageTransition(
                eq(leadId), eq(EntityType.LEAD), eq("STAGE_2"), any(), eq(true));
        when(workflowOrchestratorService.getStageConfig("STAGE_1")).thenReturn(stageConfig);
        when(userRoleService.getRolesByUsername("test-user")).thenReturn(List.of(Role.ADMIN.name()));
        when(workflowOrchestratorService.getDefaultSubStageForStage("wf-key", "STAGE_2")).thenReturn(null);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);
        when(leadStageHistoryRepositoryWrapper.save(any(LeadStageHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        LeadStageHistory result = leadStageHistoryWriteService.createStageEntry(leadIdentifier, request);

        // Assert
        assertNotNull(result, "Admin should be able to bypass role restrictions for stage transition");
        assertEquals("STAGE_2", result.getStageKey(), "Stage key should match the target stage");
    }

    @Test
    void createStageEntry_previousEntryAlreadyExited_throwsException() {
        // Arrange
        Lead.WorkflowDetails wd = Lead.WorkflowDetails.builder().workflowConfigKey("wf-key").build();
        lead.setWorkflowDetails(wd);

        LeadStageHistory exitedEntry = LeadStageHistory.builder()
                .leadId(leadId).stageKey("STAGE_1").enteredAt(LocalDateTime.now())
                .exitedAt(LocalDateTime.now()).assignmentHistory(new ArrayList<>()).build();

        PossibleNextStage nextStage = PossibleNextStage.builder()
                .stageKey("STAGE_2").allowedRoles(null).build();
        StageConfigResponse stageConfig = StageConfigResponse.builder()
                .key("STAGE_1").possibleNextStages(List.of(nextStage)).build();

        StageTransitionRequest request = StageTransitionRequest.builder()
                .stageKey("STAGE_2").build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadStageHistoryRepositoryWrapper.findLatestEntry(leadId))
                .thenReturn(Optional.of(exitedEntry));
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        doNothing().when(workflowOrchestratorService).validateStageTransition(
                eq(leadId), eq(EntityType.LEAD), eq("STAGE_2"), any(), eq(true));
        when(workflowOrchestratorService.getStageConfig("STAGE_1")).thenReturn(stageConfig);

        // Act & Assert
        assertThrows(LeadStageHistoryValidationException.class,
                () -> leadStageHistoryWriteService.createStageEntry(leadIdentifier, request),
                "Should throw when previous entry has already been exited");
    }

    @Test
    void createStageEntry_withNullPossibleNextStages_skipsTransitionValidation() {
        // Arrange — null possibleNextStages means transition is allowed
        Lead.WorkflowDetails wd = Lead.WorkflowDetails.builder().workflowConfigKey("wf-key").build();
        lead.setWorkflowDetails(wd);

        LeadStageHistory previousEntry = LeadStageHistory.builder()
                .leadId(leadId).stageKey("STAGE_1").enteredAt(LocalDateTime.now())
                .assignmentHistory(new ArrayList<>()).build();

        StageConfigResponse stageConfig = StageConfigResponse.builder()
                .key("STAGE_1").possibleNextStages(null).build();

        StageTransitionRequest request = StageTransitionRequest.builder()
                .stageKey("STAGE_2").build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadStageHistoryRepositoryWrapper.findLatestEntry(leadId))
                .thenReturn(Optional.of(previousEntry));
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        doNothing().when(workflowOrchestratorService).validateStageTransition(
                eq(leadId), eq(EntityType.LEAD), eq("STAGE_2"), any(), eq(true));
        when(workflowOrchestratorService.getStageConfig("STAGE_1")).thenReturn(stageConfig);
        when(workflowOrchestratorService.getDefaultSubStageForStage("wf-key", "STAGE_2")).thenReturn(null);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);
        when(leadStageHistoryRepositoryWrapper.save(any(LeadStageHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        LeadStageHistory result = leadStageHistoryWriteService.createStageEntry(leadIdentifier, request);

        // Assert
        assertNotNull(result, "Should succeed when possibleNextStages is null (unrestricted transition)");
    }

    @Test
    void createStageEntry_subsequentEntry_unassignsActiveAssignments() {
        // Arrange — previous entry has active assignments that should be unassigned on close
        Lead.WorkflowDetails wd = Lead.WorkflowDetails.builder().workflowConfigKey("wf-key").build();
        lead.setWorkflowDetails(wd);

        LeadStageAssignmentHistory activeAssignment = LeadStageAssignmentHistory.builder()
                .assignedTo("old-user").assignedAt(LocalDateTime.now()).unassignedAt(null).build();
        LeadStageAssignmentHistory alreadyUnassigned = LeadStageAssignmentHistory.builder()
                .assignedTo("older-user").assignedAt(LocalDateTime.now())
                .unassignedAt(LocalDateTime.now().minusHours(1)).build();

        List<LeadStageAssignmentHistory> assignments = new ArrayList<>();
        assignments.add(activeAssignment);
        assignments.add(alreadyUnassigned);

        LeadStageHistory previousEntry = LeadStageHistory.builder()
                .leadId(leadId).stageKey("STAGE_1").enteredAt(LocalDateTime.now())
                .assignmentHistory(assignments).build();

        PossibleNextStage nextStage = PossibleNextStage.builder()
                .stageKey("STAGE_2").allowedRoles(null).build();
        StageConfigResponse stageConfig = StageConfigResponse.builder()
                .key("STAGE_1").possibleNextStages(List.of(nextStage)).build();

        StageTransitionRequest request = StageTransitionRequest.builder()
                .stageKey("STAGE_2").build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadStageHistoryRepositoryWrapper.findLatestEntry(leadId))
                .thenReturn(Optional.of(previousEntry));
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        doNothing().when(workflowOrchestratorService).validateStageTransition(
                eq(leadId), eq(EntityType.LEAD), eq("STAGE_2"), any(), eq(true));
        when(workflowOrchestratorService.getStageConfig("STAGE_1")).thenReturn(stageConfig);
        when(workflowOrchestratorService.getDefaultSubStageForStage("wf-key", "STAGE_2")).thenReturn(null);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);
        when(leadStageHistoryRepositoryWrapper.save(any(LeadStageHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        leadStageHistoryWriteService.createStageEntry(leadIdentifier, request);

        // Assert
        assertNotNull(activeAssignment.getUnassignedAt(),
                "Active assignment should be unassigned when previous stage is closed");
        assertNotNull(previousEntry.getExitedAt(),
                "Previous entry should have exitedAt set when closing");
    }

    @Test
    void createStageEntry_withNullWorkflowDetailsOnLead_skipsDefaultSubStageLookup() {
        // Arrange — lead has null workflowDetails; configKey is null so getDefaultSubStageForStage returns null early
        lead.setWorkflowDetails(null);

        StageTransitionRequest request = StageTransitionRequest.builder()
                .stageKey("STAGE_1").build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadStageHistoryRepositoryWrapper.findLatestEntry(leadId)).thenReturn(Optional.empty());
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        doNothing().when(workflowOrchestratorService).validateStageTransition(
                eq(leadId), eq(EntityType.LEAD), eq("STAGE_1"), any(), eq(false));
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);
        when(leadStageHistoryRepositoryWrapper.save(any(LeadStageHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        LeadStageHistory result = leadStageHistoryWriteService.createStageEntry(leadIdentifier, request);

        // Assert
        assertNotNull(result, "Should succeed even when lead has null workflow details");
        verify(workflowOrchestratorService, never()).getDefaultSubStageForStage(any(), any());
    }

    // ==================== changeAssignment() Tests ====================

    @Test
    void changeAssignment_withNullLeadId_throwsLeadStageValidationException() {
        assertThrows(LeadStageValidationException.class,
                () -> leadStageHistoryWriteService.changeAssignment(null, "STAGE_1", "user1"),
                "Should throw when leadId is null");
        verifyNoInteractions(leadRepositoryWrapper);
    }

    @Test
    void changeAssignment_withNullStageKey_throwsLeadStageValidationException() {
        assertThrows(LeadStageValidationException.class,
                () -> leadStageHistoryWriteService.changeAssignment(leadIdentifier, null, "user1"),
                "Should throw when stageKey is null");
    }

    @Test
    void changeAssignment_withEmptyStageKey_throwsLeadStageValidationException() {
        assertThrows(LeadStageValidationException.class,
                () -> leadStageHistoryWriteService.changeAssignment(leadIdentifier, "", "user1"),
                "Should throw when stageKey is empty");
    }

    @Test
    void changeAssignment_withNullAssignedTo_throwsLeadStageValidationException() {
        assertThrows(LeadStageValidationException.class,
                () -> leadStageHistoryWriteService.changeAssignment(leadIdentifier, "STAGE_1", null),
                "Should throw when newAssignedTo is null");
    }

    @Test
    void changeAssignment_withEmptyAssignedTo_throwsLeadStageValidationException() {
        assertThrows(LeadStageValidationException.class,
                () -> leadStageHistoryWriteService.changeAssignment(leadIdentifier, "STAGE_1", ""),
                "Should throw when newAssignedTo is empty");
    }

    @Test
    void changeAssignment_withValidInputs_updatesAssignmentAndPublishesEvent() {
        // Arrange
        Lead.CurrentStageDetails currentStageDetails = Lead.CurrentStageDetails.builder()
                .stageKey("STAGE_1").assignedTo("old-user").enteredAt(LocalDateTime.now()).build();
        Lead.WorkflowDetails workflowDetails = Lead.WorkflowDetails.builder()
                .workflowConfigKey("wf-key").currentStageDetails(currentStageDetails).build();
        lead.setWorkflowDetails(workflowDetails);

        LeadStageHistory activeEntry = LeadStageHistory.builder()
                .leadId(leadId).stageKey("STAGE_1").enteredAt(LocalDateTime.now())
                .assignmentHistory(new ArrayList<>()).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        when(leadStageHistoryRepositoryWrapper.findLatestEntry(leadId)).thenReturn(Optional.of(activeEntry));
        when(leadStageHistoryRepositoryWrapper.save(any(LeadStageHistory.class))).thenReturn(activeEntry);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        LeadStageHistory result = leadStageHistoryWriteService.changeAssignment(
                leadIdentifier, "STAGE_1", "new-user");

        // Assert
        assertNotNull(result, "Result should not be null after successful assignment change");
        verify(leadStageHistoryRepositoryWrapper).save(any(LeadStageHistory.class));
        verify(leadRepositoryWrapper).saveWithException(any(Lead.class));
        verify(eventPublisher).publishEvent(any(SystemEvent.class));
    }

    @Test
    void changeAssignment_whenNoActiveStageEntry_throwsLeadStageHistoryValidationException() {
        // Arrange
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        when(leadStageHistoryRepositoryWrapper.findLatestEntry(leadId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(LeadStageHistoryValidationException.class,
                () -> leadStageHistoryWriteService.changeAssignment(leadIdentifier, "STAGE_1", "new-user"),
                "Should throw when no active stage entry exists for the lead");
    }

    @Test
    void changeAssignment_whenStageKeyMismatch_throwsLeadStageHistoryValidationException() {
        // Arrange
        LeadStageHistory activeEntry = LeadStageHistory.builder()
                .leadId(leadId).stageKey("DIFFERENT_STAGE").enteredAt(LocalDateTime.now())
                .assignmentHistory(new ArrayList<>()).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        when(leadStageHistoryRepositoryWrapper.findLatestEntry(leadId)).thenReturn(Optional.of(activeEntry));

        // Act & Assert
        assertThrows(LeadStageHistoryValidationException.class,
                () -> leadStageHistoryWriteService.changeAssignment(leadIdentifier, "STAGE_1", "new-user"),
                "Should throw when requested stage key does not match the current active stage");
    }

    @Test
    void changeAssignment_whenStageAlreadyExited_throwsLeadStageHistoryValidationException() {
        // Arrange
        LeadStageHistory exitedEntry = LeadStageHistory.builder()
                .leadId(leadId).stageKey("STAGE_1").enteredAt(LocalDateTime.now())
                .exitedAt(LocalDateTime.now()).assignmentHistory(new ArrayList<>()).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        when(leadStageHistoryRepositoryWrapper.findLatestEntry(leadId)).thenReturn(Optional.of(exitedEntry));

        // Act & Assert
        assertThrows(LeadStageHistoryValidationException.class,
                () -> leadStageHistoryWriteService.changeAssignment(leadIdentifier, "STAGE_1", "new-user"),
                "Should throw when the stage has already been exited");
    }

    @Test
    void changeAssignment_whenWorkflowDetailsNull_skipsWorkflowUpdate() {
        // Arrange — lead has no workflow details; updateLeadWorkflowDetailsAssignment returns early
        lead.setWorkflowDetails(null);

        LeadStageHistory activeEntry = LeadStageHistory.builder()
                .leadId(leadId).stageKey("STAGE_1").enteredAt(LocalDateTime.now())
                .assignmentHistory(new ArrayList<>()).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        when(leadStageHistoryRepositoryWrapper.findLatestEntry(leadId)).thenReturn(Optional.of(activeEntry));
        when(leadStageHistoryRepositoryWrapper.save(any(LeadStageHistory.class))).thenReturn(activeEntry);

        // Act
        LeadStageHistory result = leadStageHistoryWriteService.changeAssignment(
                leadIdentifier, "STAGE_1", "new-user");

        // Assert
        assertNotNull(result, "Should succeed even when workflow details are null");
        verify(leadRepositoryWrapper, never()).saveWithException(any(Lead.class));
    }

    @Test
    void changeAssignment_whenCurrentStageDetailsNull_skipsWorkflowUpdate() {
        // Arrange — workflowDetails exists but currentStageDetails is null
        Lead.WorkflowDetails wd = Lead.WorkflowDetails.builder()
                .workflowConfigKey("wf-key").currentStageDetails(null).build();
        lead.setWorkflowDetails(wd);

        LeadStageHistory activeEntry = LeadStageHistory.builder()
                .leadId(leadId).stageKey("STAGE_1").enteredAt(LocalDateTime.now())
                .assignmentHistory(new ArrayList<>()).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        when(leadStageHistoryRepositoryWrapper.findLatestEntry(leadId)).thenReturn(Optional.of(activeEntry));
        when(leadStageHistoryRepositoryWrapper.save(any(LeadStageHistory.class))).thenReturn(activeEntry);

        // Act
        LeadStageHistory result = leadStageHistoryWriteService.changeAssignment(
                leadIdentifier, "STAGE_1", "new-user");

        // Assert
        assertNotNull(result, "Should succeed even when currentStageDetails is null");
        verify(leadRepositoryWrapper, never()).saveWithException(any(Lead.class));
    }

    @Test
    void changeAssignment_withNullUsername_usesSystemAsAssignedBy() {
        // Arrange
        UserContext.setUsername(null);

        Lead.CurrentStageDetails csd = Lead.CurrentStageDetails.builder()
                .stageKey("STAGE_1").assignedTo("old-user").enteredAt(LocalDateTime.now()).build();
        Lead.WorkflowDetails wd = Lead.WorkflowDetails.builder()
                .workflowConfigKey("wf-key").currentStageDetails(csd).build();
        lead.setWorkflowDetails(wd);

        LeadStageHistory activeEntry = LeadStageHistory.builder()
                .leadId(leadId).stageKey("STAGE_1").enteredAt(LocalDateTime.now())
                .assignmentHistory(new ArrayList<>()).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        when(leadStageHistoryRepositoryWrapper.findLatestEntry(leadId)).thenReturn(Optional.of(activeEntry));
        when(leadStageHistoryRepositoryWrapper.save(any(LeadStageHistory.class))).thenReturn(activeEntry);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        LeadStageHistory result = leadStageHistoryWriteService.changeAssignment(
                leadIdentifier, "STAGE_1", "new-user");

        // Assert
        assertNotNull(result, "Should succeed with null username");
        assertFalse(activeEntry.getAssignmentHistory().isEmpty(),
                "Assignment history should have a new entry");
        assertEquals("system", activeEntry.getAssignmentHistory().get(0).getAssignedBy(),
                "assignedBy should fall back to 'system' when username is null");
    }

    // ==================== changeSubStage() Tests ====================

    @Test
    void changeSubStage_withNullLeadId_throwsLeadStageValidationException() {
        assertThrows(LeadStageValidationException.class,
                () -> leadStageHistoryWriteService.changeSubStage(null, "STAGE_1", "SUB_1"),
                "Should throw when leadId is null");
        verifyNoInteractions(leadRepositoryWrapper);
    }

    @Test
    void changeSubStage_withNullStageKey_throwsLeadStageValidationException() {
        assertThrows(LeadStageValidationException.class,
                () -> leadStageHistoryWriteService.changeSubStage(leadIdentifier, null, "SUB_1"),
                "Should throw when stageKey is null");
    }

    @Test
    void changeSubStage_withEmptyStageKey_throwsLeadStageValidationException() {
        assertThrows(LeadStageValidationException.class,
                () -> leadStageHistoryWriteService.changeSubStage(leadIdentifier, "", "SUB_1"),
                "Should throw when stageKey is empty");
    }

    @Test
    void changeSubStage_withNullSubStageKey_throwsLeadStageValidationException() {
        assertThrows(LeadStageValidationException.class,
                () -> leadStageHistoryWriteService.changeSubStage(leadIdentifier, "STAGE_1", null),
                "Should throw when subStageKey is null");
    }

    @Test
    void changeSubStage_withEmptySubStageKey_throwsLeadStageValidationException() {
        assertThrows(LeadStageValidationException.class,
                () -> leadStageHistoryWriteService.changeSubStage(leadIdentifier, "STAGE_1", ""),
                "Should throw when subStageKey is empty");
    }

    @Test
    void changeSubStage_withValidInputs_updatesSubStage() {
        // Arrange
        Lead.CurrentStageDetails currentStageDetails = Lead.CurrentStageDetails.builder()
                .stageKey("STAGE_1").subStageKey("OLD_SUB").assignedTo("user1")
                .enteredAt(LocalDateTime.now()).build();
        Lead.WorkflowDetails workflowDetails = Lead.WorkflowDetails.builder()
                .workflowConfigKey("wf-key").currentStageDetails(currentStageDetails).build();
        lead.setWorkflowDetails(workflowDetails);

        CodeValueResponse subStage = CodeValueResponse.builder().key("NEW_SUB").build();
        StageConfigResponse stageConfig = StageConfigResponse.builder()
                .key("STAGE_1").subStages(List.of(subStage)).build();

        LeadStageHistory activeEntry = LeadStageHistory.builder()
                .leadId(leadId).stageKey("STAGE_1").enteredAt(LocalDateTime.now())
                .assignmentHistory(new ArrayList<>()).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        when(workflowOrchestratorService.getStageConfig("STAGE_1")).thenReturn(stageConfig);
        when(leadStageHistoryRepositoryWrapper.findLatestEntry(leadId)).thenReturn(Optional.of(activeEntry));
        when(leadStageHistoryRepositoryWrapper.save(any(LeadStageHistory.class))).thenReturn(activeEntry);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(lead);

        // Act
        LeadStageHistory result = leadStageHistoryWriteService.changeSubStage(
                leadIdentifier, "STAGE_1", "NEW_SUB");

        // Assert
        assertNotNull(result, "Result should not be null after successful sub-stage change");
        verify(leadStageHistoryRepositoryWrapper).save(any(LeadStageHistory.class));
        verify(leadRepositoryWrapper).saveWithException(any(Lead.class));
    }

    @Test
    void changeSubStage_withInvalidSubStageKey_throwsLeadStageHistoryValidationException() {
        // Arrange
        CodeValueResponse subStage = CodeValueResponse.builder().key("VALID_SUB").build();
        StageConfigResponse stageConfig = StageConfigResponse.builder()
                .key("STAGE_1").subStages(List.of(subStage)).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        when(workflowOrchestratorService.getStageConfig("STAGE_1")).thenReturn(stageConfig);

        // Act & Assert
        assertThrows(LeadStageHistoryValidationException.class,
                () -> leadStageHistoryWriteService.changeSubStage(leadIdentifier, "STAGE_1", "INVALID_SUB"),
                "Should throw when sub-stage key is not valid for the given stage");
    }

    @Test
    void changeSubStage_withNullSubStages_throwsLeadStageHistoryValidationException() {
        // Arrange
        StageConfigResponse stageConfig = StageConfigResponse.builder()
                .key("STAGE_1").subStages(null).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        when(workflowOrchestratorService.getStageConfig("STAGE_1")).thenReturn(stageConfig);

        // Act & Assert
        assertThrows(LeadStageHistoryValidationException.class,
                () -> leadStageHistoryWriteService.changeSubStage(leadIdentifier, "STAGE_1", "ANY_SUB"),
                "Should throw when stage has no sub-stages configured");
    }

    @Test
    void changeSubStage_whenWorkflowDetailsNull_skipsWorkflowUpdate() {
        // Arrange — lead has no workflow details; updateLeadWorkflowDetailsSubStage returns early
        lead.setWorkflowDetails(null);

        CodeValueResponse subStage = CodeValueResponse.builder().key("NEW_SUB").build();
        StageConfigResponse stageConfig = StageConfigResponse.builder()
                .key("STAGE_1").subStages(List.of(subStage)).build();

        LeadStageHistory activeEntry = LeadStageHistory.builder()
                .leadId(leadId).stageKey("STAGE_1").enteredAt(LocalDateTime.now())
                .assignmentHistory(new ArrayList<>()).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        when(workflowOrchestratorService.getStageConfig("STAGE_1")).thenReturn(stageConfig);
        when(leadStageHistoryRepositoryWrapper.findLatestEntry(leadId)).thenReturn(Optional.of(activeEntry));
        when(leadStageHistoryRepositoryWrapper.save(any(LeadStageHistory.class))).thenReturn(activeEntry);

        // Act
        LeadStageHistory result = leadStageHistoryWriteService.changeSubStage(
                leadIdentifier, "STAGE_1", "NEW_SUB");

        // Assert
        assertNotNull(result, "Should succeed even when workflow details are null");
        verify(leadRepositoryWrapper, never()).saveWithException(any(Lead.class));
    }

    @Test
    void changeSubStage_whenCurrentStageDetailsNull_skipsWorkflowUpdate() {
        // Arrange — workflowDetails exists but currentStageDetails is null
        Lead.WorkflowDetails wd = Lead.WorkflowDetails.builder()
                .workflowConfigKey("wf-key").currentStageDetails(null).build();
        lead.setWorkflowDetails(wd);

        CodeValueResponse subStage = CodeValueResponse.builder().key("NEW_SUB").build();
        StageConfigResponse stageConfig = StageConfigResponse.builder()
                .key("STAGE_1").subStages(List.of(subStage)).build();

        LeadStageHistory activeEntry = LeadStageHistory.builder()
                .leadId(leadId).stageKey("STAGE_1").enteredAt(LocalDateTime.now())
                .assignmentHistory(new ArrayList<>()).build();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(leadRepositoryWrapper.findByIdWithException(leadId)).thenReturn(lead);
        when(workflowOrchestratorService.getStageConfig("STAGE_1")).thenReturn(stageConfig);
        when(leadStageHistoryRepositoryWrapper.findLatestEntry(leadId)).thenReturn(Optional.of(activeEntry));
        when(leadStageHistoryRepositoryWrapper.save(any(LeadStageHistory.class))).thenReturn(activeEntry);

        // Act
        LeadStageHistory result = leadStageHistoryWriteService.changeSubStage(
                leadIdentifier, "STAGE_1", "NEW_SUB");

        // Assert
        assertNotNull(result, "Should succeed even when currentStageDetails is null");
        verify(leadRepositoryWrapper, never()).saveWithException(any(Lead.class));
    }

    // ==================== bulkChangeAssignment() Tests ====================

    @Test
    void bulkChangeAssignment_withNullRequest_throwsLeadStageValidationException() {
        assertThrows(LeadStageValidationException.class,
                () -> leadStageHistoryWriteService.bulkChangeAssignment(null),
                "Should throw when request is null");
    }

    @Test
    void bulkChangeAssignment_withNullStageKey_throwsLeadStageValidationException() {
        BulkChangeAssignmentRequest request = BulkChangeAssignmentRequest.builder()
                .stageKey(null).newAssignedTo("user1")
                .leadIdentifiers(List.of(UUID.randomUUID())).build();

        assertThrows(LeadStageValidationException.class,
                () -> leadStageHistoryWriteService.bulkChangeAssignment(request),
                "Should throw when stageKey is null in bulk request");
    }

    @Test
    void bulkChangeAssignment_withNullAssignedTo_throwsLeadStageValidationException() {
        BulkChangeAssignmentRequest request = BulkChangeAssignmentRequest.builder()
                .stageKey("STAGE_1").newAssignedTo(null)
                .leadIdentifiers(List.of(UUID.randomUUID())).build();

        assertThrows(LeadStageValidationException.class,
                () -> leadStageHistoryWriteService.bulkChangeAssignment(request),
                "Should throw when newAssignedTo is null in bulk request");
    }

    @Test
    void bulkChangeAssignment_withNullLeadIdentifiers_throwsLeadStageValidationException() {
        BulkChangeAssignmentRequest request = BulkChangeAssignmentRequest.builder()
                .stageKey("STAGE_1").newAssignedTo("user1")
                .leadIdentifiers(null).build();

        assertThrows(LeadStageValidationException.class,
                () -> leadStageHistoryWriteService.bulkChangeAssignment(request),
                "Should throw when leadIdentifiers is null in bulk request");
    }

    @Test
    void bulkChangeAssignment_withEmptyLeadIdentifiers_throwsLeadStageValidationException() {
        BulkChangeAssignmentRequest request = BulkChangeAssignmentRequest.builder()
                .stageKey("STAGE_1").newAssignedTo("user1")
                .leadIdentifiers(List.of()).build();

        assertThrows(LeadStageValidationException.class,
                () -> leadStageHistoryWriteService.bulkChangeAssignment(request),
                "Should throw when leadIdentifiers is empty in bulk request");
    }

    @Test
    void bulkChangeAssignment_whenAllSucceed_returnsCorrectCounts() {
        // Arrange
        UUID leadId1 = UUID.randomUUID();
        UUID leadId2 = UUID.randomUUID();

        Lead lead1 = buildLeadWithWorkflowDetails(1L, leadId1, "STAGE_1", "old-user");
        Lead lead2 = buildLeadWithWorkflowDetails(2L, leadId2, "STAGE_1", "old-user");

        LeadStageHistory activeEntry1 = buildActiveStageHistory(1L, "STAGE_1");
        LeadStageHistory activeEntry2 = buildActiveStageHistory(2L, "STAGE_1");

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadId1)).thenReturn(lead1);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadId2)).thenReturn(lead2);
        when(leadRepositoryWrapper.findByIdWithException(1L)).thenReturn(lead1);
        when(leadRepositoryWrapper.findByIdWithException(2L)).thenReturn(lead2);
        when(leadStageHistoryRepositoryWrapper.findLatestEntry(1L)).thenReturn(Optional.of(activeEntry1));
        when(leadStageHistoryRepositoryWrapper.findLatestEntry(2L)).thenReturn(Optional.of(activeEntry2));
        when(leadStageHistoryRepositoryWrapper.save(any(LeadStageHistory.class)))
                .thenReturn(activeEntry1).thenReturn(activeEntry2);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class)))
                .thenReturn(lead1).thenReturn(lead2);

        BulkChangeAssignmentRequest request = BulkChangeAssignmentRequest.builder()
                .stageKey("STAGE_1").newAssignedTo("new-user")
                .leadIdentifiers(List.of(leadId1, leadId2)).build();

        // Act
        BulkChangeAssignmentResponse result = leadStageHistoryWriteService.bulkChangeAssignment(request);

        // Assert
        assertEquals(2, result.getTotalRequested(), "Total requested should match input count");
        assertEquals(2, result.getSuccessful(), "All assignments should succeed");
        assertEquals(0, result.getFailed(), "No assignments should fail");
        assertTrue(result.getErrors().isEmpty(), "Errors list should be empty");
        assertTrue(result.getSuccessfulLeadIdentifiers().contains(leadId1),
                "First lead identifier should be in successful list");
        assertTrue(result.getSuccessfulLeadIdentifiers().contains(leadId2),
                "Second lead identifier should be in successful list");
    }

    @Test
    void bulkChangeAssignment_withMixedResults_returnsSuccessAndErrors() {
        // Arrange
        UUID successLeadId = UUID.randomUUID();
        UUID failLeadId = UUID.randomUUID();

        Lead successLead = buildLeadWithWorkflowDetails(1L, successLeadId, "STAGE_1", "old-user");
        LeadStageHistory activeEntry = buildActiveStageHistory(1L, "STAGE_1");

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(successLeadId)).thenReturn(successLead);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(failLeadId))
                .thenThrow(new RuntimeException("Lead not found"));
        when(leadRepositoryWrapper.findByIdWithException(1L)).thenReturn(successLead);
        when(leadStageHistoryRepositoryWrapper.findLatestEntry(1L)).thenReturn(Optional.of(activeEntry));
        when(leadStageHistoryRepositoryWrapper.save(any(LeadStageHistory.class))).thenReturn(activeEntry);
        when(leadRepositoryWrapper.saveWithException(any(Lead.class))).thenReturn(successLead);

        BulkChangeAssignmentRequest request = BulkChangeAssignmentRequest.builder()
                .stageKey("STAGE_1").newAssignedTo("new-user")
                .leadIdentifiers(List.of(successLeadId, failLeadId)).build();

        // Act
        BulkChangeAssignmentResponse result = leadStageHistoryWriteService.bulkChangeAssignment(request);

        // Assert
        assertEquals(2, result.getTotalRequested(), "Total requested should match input count");
        assertEquals(1, result.getSuccessful(), "One assignment should succeed");
        assertEquals(1, result.getFailed(), "One assignment should fail");
        assertEquals(1, result.getErrors().size(), "Errors list should contain one error");
        assertEquals(failLeadId, result.getErrors().get(0).getLeadIdentifier(),
                "Error should reference the failed lead identifier");
    }

    @Test
    void bulkChangeAssignment_whenAllFail_returnsZeroSuccessful() {
        // Arrange
        UUID failLeadId1 = UUID.randomUUID();
        UUID failLeadId2 = UUID.randomUUID();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(failLeadId1))
                .thenThrow(new RuntimeException("Not found"));
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(failLeadId2))
                .thenThrow(new RuntimeException("Not found"));

        BulkChangeAssignmentRequest request = BulkChangeAssignmentRequest.builder()
                .stageKey("STAGE_1").newAssignedTo("new-user")
                .leadIdentifiers(List.of(failLeadId1, failLeadId2)).build();

        // Act
        BulkChangeAssignmentResponse result = leadStageHistoryWriteService.bulkChangeAssignment(request);

        // Assert
        assertEquals(2, result.getTotalRequested(), "Total requested should match input count");
        assertEquals(0, result.getSuccessful(), "No assignments should succeed");
        assertEquals(2, result.getFailed(), "All assignments should fail");
        assertEquals(2, result.getErrors().size(), "Errors list should contain two errors");
    }

    @Test
    void bulkChangeAssignment_whenExceptionHasNullMessage_usesUnknownError() {
        // Arrange
        UUID failLeadId = UUID.randomUUID();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(failLeadId))
                .thenThrow(new RuntimeException((String) null));

        BulkChangeAssignmentRequest request = BulkChangeAssignmentRequest.builder()
                .stageKey("STAGE_1").newAssignedTo("new-user")
                .leadIdentifiers(List.of(failLeadId)).build();

        // Act
        BulkChangeAssignmentResponse result = leadStageHistoryWriteService.bulkChangeAssignment(request);

        // Assert
        assertEquals(1, result.getFailed(), "Assignment should fail");
        assertEquals("Unknown error", result.getErrors().get(0).getErrorMessage(),
                "Should use 'Unknown error' when exception message is null");
    }

    // ==================== Helper Methods ====================

    private Lead buildLeadWithWorkflowDetails(Long id, UUID identifier, String stageKey, String assignedTo) {
        Lead l = new Lead();
        l.setId(id);
        l.setLeadIdentifier(identifier);
        Lead.CurrentStageDetails csd = Lead.CurrentStageDetails.builder()
                .stageKey(stageKey).assignedTo(assignedTo).enteredAt(LocalDateTime.now()).build();
        Lead.WorkflowDetails wd = Lead.WorkflowDetails.builder()
                .workflowConfigKey("wf-key").currentStageDetails(csd).build();
        l.setWorkflowDetails(wd);
        return l;
    }

    private LeadStageHistory buildActiveStageHistory(Long leadId, String stageKey) {
        return LeadStageHistory.builder()
                .leadId(leadId).stageKey(stageKey).enteredAt(LocalDateTime.now())
                .assignmentHistory(new ArrayList<>()).build();
    }
}
