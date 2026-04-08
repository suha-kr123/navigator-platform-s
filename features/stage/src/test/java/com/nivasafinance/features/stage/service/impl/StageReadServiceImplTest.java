package com.nivasafinance.features.stage.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeMasterService;
import com.nivasafinance.features.rolemanagement.enums.Role;
import com.nivasafinance.features.rolemanagement.role.dto.UserAssignmentResponse;
import com.nivasafinance.features.rolemanagement.role.service.UserQueryService;
import com.nivasafinance.features.rolemanagement.role.service.UserRoleService;
import com.nivasafinance.features.stage.dto.StageConfigResponse;
import com.nivasafinance.features.stage.dto.StageFilterResponse;
import com.nivasafinance.features.stage.dto.StageTemplateResponse;
import com.nivasafinance.features.stage.entity.StageConfig;
import com.nivasafinance.features.stage.exception.StageConfigNotFoundException;
import com.nivasafinance.features.stage.repository.StageConfigRepositoryWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StageReadServiceImplTest {

    @Mock
    private StageConfigRepositoryWrapper stageConfigRepositoryWrapper;

    @Mock
    private CodeMasterService codeMasterService;

    @Mock
    private UserQueryService userQueryService;

    @Mock
    private UserRoleService userRoleService;

    @Mock
    private ApplicationContext applicationContext;

    @InjectMocks
    private StageReadServiceImpl service;

    @BeforeEach
    void setUp() {
        service.setApplicationContext(applicationContext);
        UserContext.setUsername("testuser");
    }

    @AfterEach
    void tearDown() {
        UserContext.setUsername(null);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getStageByKey: happy path
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void getStageByKey_withFullConfig_returnsPopulatedResponse() {
        StageConfig config = buildStageConfig("STG", "Stage One",
                List.of(buildNextStage("NEXT_1", List.of("CSE"))),
                List.of("BM"), "SUB_CODE");
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG")).thenReturn(config);
        when(userRoleService.getRolesByUsername("testuser")).thenReturn(List.of("CSE"));
        when(codeMasterService.getAllCodeValuesByCodeKey("SUB_CODE", true, "default"))
                .thenReturn(List.of(new CodeValueResponse()));

        StageConfigResponse result = service.getStageByKey("STG");

        assertEquals("STG", result.getKey(), "Response key should match the stage config key");
        assertEquals("Stage One", result.getName(), "Response name should match the stage config name");
        assertEquals(List.of("NEXT_1"), result.getPossibleNextStages(),
                "Possible next stages should contain keys the user's role has access to");
        assertEquals(List.of("BM"), result.getAssigneeRoles(),
                "Assignee roles should come from stage config");
        assertEquals(1, result.getSubStages().size(),
                "Sub stages should contain values from code master");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getStageByKey: null guards on stageConfig / possibleNextStages
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void getStageByKey_whenStageConfigDetailsNull_returnsEmptyPossibleNextStages() {
        StageConfig config = buildStageConfig("STG", "Stage", null, null, null);
        config.setStageConfig(null);
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG")).thenReturn(config);

        StageConfigResponse result = service.getStageByKey("STG");

        assertTrue(result.getPossibleNextStages().isEmpty(),
                "Possible next stages should be empty when stage config details are null");
    }

    @Test
    void getStageByKey_whenPossibleNextStagesNull_returnsEmptyList() {
        StageConfig config = buildStageConfig("STG", "Stage", null, null, null);
        config.setStageConfig(StageConfig.StageConfigDetails.builder().possibleNextStages(null).build());
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG")).thenReturn(config);

        StageConfigResponse result = service.getStageByKey("STG");

        assertTrue(result.getPossibleNextStages().isEmpty(),
                "Possible next stages should be empty when the nested list is null");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getStageByKey: null guards on assigneeRoles
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void getStageByKey_whenAssigneeRolesNull_returnsEmptyAssigneeRoles() {
        StageConfig config = buildStageConfig("STG", "Stage", null, null, null);
        config.setAssigneeRoles(null);
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG")).thenReturn(config);

        StageConfigResponse result = service.getStageByKey("STG");

        assertTrue(result.getAssigneeRoles().isEmpty(),
                "Assignee roles should be empty when assigneeRoles entity is null");
    }

    @Test
    void getStageByKey_whenAssigneeRolesRolesListNull_returnsEmptyAssigneeRoles() {
        StageConfig config = buildStageConfig("STG", "Stage", null, null, null);
        config.setAssigneeRoles(StageConfig.AssigneeRoles.builder().roles(null).build());
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG")).thenReturn(config);

        StageConfigResponse result = service.getStageByKey("STG");

        assertTrue(result.getAssigneeRoles().isEmpty(),
                "Assignee roles should be empty when roles list inside assigneeRoles is null");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getStageByKey: fetchSubStages branches
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void getStageByKey_whenSubStagesCodeNull_returnsEmptySubStages() {
        StageConfig config = buildStageConfig("STG", "Stage", null, null, null);
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG")).thenReturn(config);

        StageConfigResponse result = service.getStageByKey("STG");

        assertTrue(result.getSubStages().isEmpty(),
                "Sub stages should be empty when subStagesCode is null");
        verifyNoInteractions(codeMasterService);
    }

    @Test
    void getStageByKey_whenSubStagesCodeEmpty_returnsEmptySubStages() {
        StageConfig config = buildStageConfig("STG", "Stage", null, null, "");
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG")).thenReturn(config);

        StageConfigResponse result = service.getStageByKey("STG");

        assertTrue(result.getSubStages().isEmpty(),
                "Sub stages should be empty when subStagesCode is empty string");
        verifyNoInteractions(codeMasterService);
    }

    @Test
    void getStageByKey_whenSubStagesCodeBlank_returnsEmptySubStages() {
        StageConfig config = buildStageConfig("STG", "Stage", null, null, "   ");
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG")).thenReturn(config);

        StageConfigResponse result = service.getStageByKey("STG");

        assertTrue(result.getSubStages().isEmpty(),
                "Sub stages should be empty when subStagesCode is blank");
        verifyNoInteractions(codeMasterService);
    }

    @Test
    void getStageByKey_whenCodeMasterReturnsNull_returnsEmptySubStages() {
        StageConfig config = buildStageConfig("STG", "Stage", null, null, "CODE");
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG")).thenReturn(config);
        when(codeMasterService.getAllCodeValuesByCodeKey("CODE", true, "default")).thenReturn(null);

        StageConfigResponse result = service.getStageByKey("STG");

        assertTrue(result.getSubStages().isEmpty(),
                "Sub stages should be empty when code master returns null");
    }

    @Test
    void getStageByKey_whenCodeMasterThrowsException_returnsEmptySubStages() {
        StageConfig config = buildStageConfig("STG", "Stage", null, null, "CODE");
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG")).thenReturn(config);
        when(codeMasterService.getAllCodeValuesByCodeKey("CODE", true, "default"))
                .thenThrow(new RuntimeException("code master error"));

        StageConfigResponse result = service.getStageByKey("STG");

        assertTrue(result.getSubStages().isEmpty(),
                "Sub stages should be empty when code master throws exception");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getStageByKey: filterByCurrentUserRole
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void getStageByKey_whenEmptyPossibleNextStages_returnsEmptyWithoutRoleLookup() {
        StageConfig config = buildStageConfig("STG", "Stage",
                Collections.emptyList(), null, null);
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG")).thenReturn(config);

        StageConfigResponse result = service.getStageByKey("STG");

        assertTrue(result.getPossibleNextStages().isEmpty(),
                "Should return empty list when possible next stages list is empty");
        verifyNoInteractions(userRoleService);
    }

    @Test
    void getStageByKey_whenUsernameNull_returnsEmptyPossibleNextStages() {
        UserContext.setUsername(null);
        StageConfig config = buildStageConfig("STG", "Stage",
                List.of(buildNextStage("NEXT", List.of("CSE"))), null, null);
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG")).thenReturn(config);

        StageConfigResponse result = service.getStageByKey("STG");

        assertTrue(result.getPossibleNextStages().isEmpty(),
                "Should return empty possible next stages when username is null");
    }

    @Test
    void getStageByKey_whenUserHasNoRoles_returnsEmptyPossibleNextStages() {
        StageConfig config = buildStageConfig("STG", "Stage",
                List.of(buildNextStage("NEXT", List.of("CSE"))), null, null);
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG")).thenReturn(config);
        when(userRoleService.getRolesByUsername("testuser")).thenReturn(Collections.emptyList());

        StageConfigResponse result = service.getStageByKey("STG");

        assertTrue(result.getPossibleNextStages().isEmpty(),
                "Should return empty possible next stages when user has no roles");
    }

    @Test
    void getStageByKey_whenUserIsAdmin_returnsAllPossibleNextStages() {
        StageConfig config = buildStageConfig("STG", "Stage",
                List.of(buildNextStage("NEXT_1", List.of("CSE")),
                        buildNextStage("NEXT_2", List.of("BM"))),
                null, null);
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG")).thenReturn(config);
        when(userRoleService.getRolesByUsername("testuser")).thenReturn(List.of(Role.ADMIN.name()));

        StageConfigResponse result = service.getStageByKey("STG");

        assertEquals(2, result.getPossibleNextStages().size(),
                "ADMIN user should see all possible next stages regardless of allowed roles");
    }

    @Test
    void getStageByKey_whenUserRoleMatchesOneStage_returnsOnlyMatchingStage() {
        StageConfig config = buildStageConfig("STG", "Stage",
                List.of(buildNextStage("NEXT_1", List.of("CSE")),
                        buildNextStage("NEXT_2", List.of("BM"))),
                null, null);
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG")).thenReturn(config);
        when(userRoleService.getRolesByUsername("testuser")).thenReturn(List.of("CSE"));

        StageConfigResponse result = service.getStageByKey("STG");

        assertEquals(List.of("NEXT_1"), result.getPossibleNextStages(),
                "Should only return stages where user's role is in the allowed roles");
    }

    @Test
    void getStageByKey_whenStageHasNullAllowedRoles_includesStage() {
        StageConfig config = buildStageConfig("STG", "Stage",
                List.of(buildNextStage("NEXT_1", null),
                        buildNextStage("NEXT_2", List.of("BM"))),
                null, null);
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG")).thenReturn(config);
        when(userRoleService.getRolesByUsername("testuser")).thenReturn(List.of("CSE"));

        StageConfigResponse result = service.getStageByKey("STG");

        assertEquals(List.of("NEXT_1"), result.getPossibleNextStages(),
                "Stages with null allowedRoles should be accessible to any role");
    }

    @Test
    void getStageByKey_whenStageHasEmptyAllowedRoles_includesStage() {
        StageConfig config = buildStageConfig("STG", "Stage",
                List.of(buildNextStage("NEXT_1", Collections.emptyList()),
                        buildNextStage("NEXT_2", List.of("BM"))),
                null, null);
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG")).thenReturn(config);
        when(userRoleService.getRolesByUsername("testuser")).thenReturn(List.of("CSE"));

        StageConfigResponse result = service.getStageByKey("STG");

        assertEquals(List.of("NEXT_1"), result.getPossibleNextStages(),
                "Stages with empty allowedRoles should be accessible to any role");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getStageTemplate
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void getStageTemplate_withValidKey_returnsMappedTemplate() {
        StageConfig config = buildStageConfig("STG", "Stage One",
                Collections.emptyList(), List.of("BM"), "SUB_CODE");
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG")).thenReturn(config);
        when(codeMasterService.getAllCodeValuesByCodeKey("SUB_CODE", true, "default"))
                .thenReturn(List.of(new CodeValueResponse()));

        StageTemplateResponse result = service.getStageTemplate("STG");

        assertEquals("STG", result.getStageKey(), "Template stage key should match input");
        assertEquals("Stage One", result.getStageName(), "Template stage name should match config");
        assertNotNull(result.getAvailableSubStages(),
                "Available sub stages should not be null");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getStageTemplates
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void getStageTemplates_withNullStageKeys_returnsEmptyMap() {
        Map<String, StageTemplateResponse> result = service.getStageTemplates(null);

        assertTrue(result.isEmpty(), "Should return empty map when stageKeys is null");
    }

    @Test
    void getStageTemplates_withEmptyStageKeys_returnsEmptyMap() {
        Map<String, StageTemplateResponse> result = service.getStageTemplates(Collections.emptyList());

        assertTrue(result.isEmpty(), "Should return empty map when stageKeys is empty");
    }

    @Test
    void getStageTemplates_withValidKeys_returnsPopulatedMap() {
        StageConfig config = buildStageConfig("STG", "Stage", Collections.emptyList(), null, null);
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG")).thenReturn(config);

        Map<String, StageTemplateResponse> result = service.getStageTemplates(List.of("STG"));

        assertEquals(1, result.size(), "Map should contain one entry for the valid key");
        assertNotNull(result.get("STG"), "Map should contain entry for key 'STG'");
    }

    @Test
    void getStageTemplates_withBlankAndNullKeys_filtersInvalidKeys() {
        List<String> keys = new ArrayList<>();
        keys.add(null);
        keys.add("  ");
        keys.add("");

        Map<String, StageTemplateResponse> result = service.getStageTemplates(keys);

        assertTrue(result.isEmpty(),
                "Should return empty map when all keys are null or blank after processing");
    }

    @Test
    void getStageTemplates_withDuplicateKeys_deduplicates() {
        StageConfig config = buildStageConfig("STG", "Stage", Collections.emptyList(), null, null);
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG")).thenReturn(config);

        Map<String, StageTemplateResponse> result = service.getStageTemplates(List.of("STG", "STG"));

        assertEquals(1, result.size(), "Should deduplicate stage keys and return one entry");
    }

    @Test
    void getStageTemplates_whenOneKeyNotFound_skipsAndContinues() {
        StageConfig config = buildStageConfig("STG_1", "Stage 1", Collections.emptyList(), null, null);
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG_1")).thenReturn(config);
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG_BAD"))
                .thenThrow(mock(StageConfigNotFoundException.class));

        Map<String, StageTemplateResponse> result = service.getStageTemplates(List.of("STG_1", "STG_BAD"));

        assertEquals(1, result.size(), "Should skip not-found stage and include only valid one");
        assertNotNull(result.get("STG_1"), "Valid stage STG_1 should be present in result");
    }

    @Test
    void getStageTemplates_whenOneKeyThrowsException_skipsAndContinues() {
        StageConfig config = buildStageConfig("STG_1", "Stage 1", Collections.emptyList(), null, null);
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG_1")).thenReturn(config);
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG_ERR"))
                .thenThrow(new RuntimeException("unexpected"));

        Map<String, StageTemplateResponse> result = service.getStageTemplates(List.of("STG_1", "STG_ERR"));

        assertEquals(1, result.size(), "Should skip errored stage and include only valid one");
    }

    @Test
    void getStageTemplates_keysWithWhitespace_trimmedBeforeLookup() {
        StageConfig config = buildStageConfig("STG", "Stage", Collections.emptyList(), null, null);
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG")).thenReturn(config);

        Map<String, StageTemplateResponse> result = service.getStageTemplates(List.of("  STG  "));

        assertEquals(1, result.size(), "Should trim whitespace from keys before lookup");
        assertNotNull(result.get("STG"), "Trimmed key 'STG' should be present in result");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getAssignableUsersForStages
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void getAssignableUsersForStages_withNullStageKeys_returnsEmptyList() {
        List<UserAssignmentResponse> result = service.getAssignableUsersForStages(null, "OFF_1");

        assertTrue(result.isEmpty(), "Should return empty list when stageKeys is null");
    }

    @Test
    void getAssignableUsersForStages_withEmptyStageKeys_returnsEmptyList() {
        List<UserAssignmentResponse> result = service.getAssignableUsersForStages(Collections.emptyList(), "OFF_1");

        assertTrue(result.isEmpty(), "Should return empty list when stageKeys is empty");
    }

    @Test
    void getAssignableUsersForStages_withNullOfficeKey_returnsEmptyList() {
        List<UserAssignmentResponse> result = service.getAssignableUsersForStages(List.of("STG"), null);

        assertTrue(result.isEmpty(), "Should return empty list when officeKey is null");
    }

    @Test
    void getAssignableUsersForStages_whenNoRolesFromStages_returnsEmptyList() {
        StageConfig config = buildStageConfig("STG", "Stage", Collections.emptyList(), null, null);
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG")).thenReturn(config);

        List<UserAssignmentResponse> result = service.getAssignableUsersForStages(List.of("STG"), "OFF_1");

        assertTrue(result.isEmpty(),
                "Should return empty list when no assignee roles are configured for stages");
    }

    @Test
    void getAssignableUsersForStages_withValidRoles_returnsDedupedUsers() {
        StageConfig config = buildStageConfig("STG", "Stage", Collections.emptyList(),
                List.of("CSE", "BM"), null);
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG")).thenReturn(config);

        UserAssignmentResponse user1 = UserAssignmentResponse.builder().username("user1").name("User One").build();
        UserAssignmentResponse user1Dup = UserAssignmentResponse.builder().username("user1").name("User One Dup").build();
        UserAssignmentResponse user2 = UserAssignmentResponse.builder().username("user2").name("User Two").build();
        when(userQueryService.getUsersByOfficeAndRoles(anyList(), eq("OFF_1")))
                .thenReturn(List.of(user1, user1Dup, user2));

        List<UserAssignmentResponse> result = service.getAssignableUsersForStages(List.of("STG"), "OFF_1");

        assertEquals(2, result.size(), "Duplicate users by username should be deduplicated");
    }

    @Test
    void getAssignableUsersForStages_whenUserQueryReturnsNull_returnsEmptyList() {
        StageConfig config = buildStageConfig("STG", "Stage", Collections.emptyList(),
                List.of("CSE"), null);
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG")).thenReturn(config);
        when(userQueryService.getUsersByOfficeAndRoles(anyList(), eq("OFF_1"))).thenReturn(null);

        List<UserAssignmentResponse> result = service.getAssignableUsersForStages(List.of("STG"), "OFF_1");

        assertTrue(result.isEmpty(), "Should return empty list when user query returns null");
    }

    @Test
    void getAssignableUsersForStages_whenUserQueryReturnsEmpty_returnsEmptyList() {
        StageConfig config = buildStageConfig("STG", "Stage", Collections.emptyList(),
                List.of("CSE"), null);
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG")).thenReturn(config);
        when(userQueryService.getUsersByOfficeAndRoles(anyList(), eq("OFF_1")))
                .thenReturn(Collections.emptyList());

        List<UserAssignmentResponse> result = service.getAssignableUsersForStages(List.of("STG"), "OFF_1");

        assertTrue(result.isEmpty(), "Should return empty list when user query returns no users");
    }

    @Test
    void getAssignableUsersForStages_whenExceptionThrown_returnsEmptyList() {
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG"))
                .thenThrow(new RuntimeException("unexpected"));

        List<UserAssignmentResponse> result = service.getAssignableUsersForStages(List.of("STG"), "OFF_1");

        assertTrue(result.isEmpty(), "Should return empty list when unexpected exception occurs");
    }

    @Test
    void getAssignableUsersForStages_collectsRolesFromMultipleStages() {
        StageConfig config1 = buildStageConfig("STG_1", "Stage 1", Collections.emptyList(),
                List.of("CSE"), null);
        StageConfig config2 = buildStageConfig("STG_2", "Stage 2", Collections.emptyList(),
                List.of("BM"), null);
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG_1")).thenReturn(config1);
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG_2")).thenReturn(config2);

        UserAssignmentResponse user = UserAssignmentResponse.builder().username("user1").build();
        when(userQueryService.getUsersByOfficeAndRoles(anyList(), eq("OFF_1")))
                .thenReturn(List.of(user));

        List<UserAssignmentResponse> result = service.getAssignableUsersForStages(
                List.of("STG_1", "STG_2"), "OFF_1");

        assertEquals(1, result.size(), "Should return users matching roles from all provided stages");
        verify(userQueryService).getUsersByOfficeAndRoles(anyList(), eq("OFF_1"));
    }

    @Test
    void getAssignableUsersForStages_whenOneStageThrowsInRoleCollection_skipsAndContinues() {
        StageConfig config1 = buildStageConfig("STG_1", "Stage 1", Collections.emptyList(),
                List.of("CSE"), null);
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG_1")).thenReturn(config1);
        when(stageConfigRepositoryWrapper.findByKeyWithException("STG_BAD"))
                .thenThrow(new RuntimeException("bad stage"));

        UserAssignmentResponse user = UserAssignmentResponse.builder().username("user1").build();
        when(userQueryService.getUsersByOfficeAndRoles(anyList(), eq("OFF_1")))
                .thenReturn(List.of(user));

        List<UserAssignmentResponse> result = service.getAssignableUsersForStages(
                List.of("STG_1", "STG_BAD"), "OFF_1");

        assertEquals(1, result.size(),
                "Should still return users from valid stages when one stage throws during role collection");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getAssignableUsersForStagesByCurrentUser
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void getAssignableUsersForStagesByCurrentUser_whenUsernameNull_returnsEmptyList() {
        UserContext.setUsername(null);

        List<UserAssignmentResponse> result = service.getAssignableUsersForStagesByCurrentUser(List.of("STG"));

        assertTrue(result.isEmpty(),
                "Should return empty list when current username is null");
    }

    @Test
    void getAssignableUsersForStagesByCurrentUser_whenUserBeanNotFound_returnsEmptyList() {
        when(applicationContext.getBean("userReadServiceImpl"))
                .thenThrow(new RuntimeException("bean not found"));

        List<UserAssignmentResponse> result = service.getAssignableUsersForStagesByCurrentUser(List.of("STG"));

        assertTrue(result.isEmpty(),
                "Should return empty list when user read service bean is not available");
    }

    @Test
    void getAssignableUsersForStagesByCurrentUser_withEmptyStageKeys_returnsEmptyList() {
        List<UserAssignmentResponse> result = service.getAssignableUsersForStagesByCurrentUser(Collections.emptyList());

        assertTrue(result.isEmpty(),
                "Should return empty list when stage keys are empty");
    }

    @Test
    void getAssignableUsersForStagesByCurrentUser_whenExceptionThrown_returnsEmptyList() {
        when(applicationContext.getBean("userReadServiceImpl"))
                .thenThrow(new RuntimeException("unexpected"));

        List<UserAssignmentResponse> result = service.getAssignableUsersForStagesByCurrentUser(List.of("STG"));

        assertTrue(result.isEmpty(),
                "Should return empty list when an unexpected error occurs");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  getAllActiveStages
    // ═══════════════════════════════════════════════════════════════════

    @Test
    void getAllActiveStages_whenStagesExist_returnsMappedFilterResponses() {
        StageConfig config1 = new StageConfig();
        config1.setKey("STG_1");
        config1.setName("Stage 1");
        config1.setDescription("Desc 1");
        StageConfig config2 = new StageConfig();
        config2.setKey("STG_2");
        config2.setName("Stage 2");
        config2.setDescription("Desc 2");
        when(stageConfigRepositoryWrapper.findAllActiveStages()).thenReturn(List.of(config1, config2));

        List<StageFilterResponse> result = service.getAllActiveStages();

        assertEquals(2, result.size(), "Should return all active stages mapped to filter responses");
        assertEquals("STG_1", result.get(0).getKey(), "First stage key should match");
        assertEquals("STG_2", result.get(1).getKey(), "Second stage key should match");
    }

    @Test
    void getAllActiveStages_whenNoStages_returnsEmptyList() {
        when(stageConfigRepositoryWrapper.findAllActiveStages()).thenReturn(Collections.emptyList());

        List<StageFilterResponse> result = service.getAllActiveStages();

        assertTrue(result.isEmpty(), "Should return empty list when no active stages exist");
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Helpers
    // ═══════════════════════════════════════════════════════════════════

    private StageConfig buildStageConfig(String key, String name,
            List<StageConfig.PossibleNextStage> possibleNextStages,
            List<String> assigneeRoles, String subStagesCode) {
        StageConfig config = new StageConfig();
        config.setKey(key);
        config.setName(name);
        config.setDescription("Description for " + name);
        config.setIsActive(true);

        if (possibleNextStages != null) {
            config.setStageConfig(StageConfig.StageConfigDetails.builder()
                    .possibleNextStages(possibleNextStages).build());
        } else {
            config.setStageConfig(null);
        }

        if (assigneeRoles != null) {
            config.setAssigneeRoles(StageConfig.AssigneeRoles.builder().roles(assigneeRoles).build());
        } else {
            config.setAssigneeRoles(null);
        }

        config.setSubStagesCode(subStagesCode);
        return config;
    }

    private StageConfig.PossibleNextStage buildNextStage(String stageKey, List<String> allowedRoles) {
        return StageConfig.PossibleNextStage.builder()
                .stageKey(stageKey)
                .allowedRoles(allowedRoles)
                .build();
    }
}
