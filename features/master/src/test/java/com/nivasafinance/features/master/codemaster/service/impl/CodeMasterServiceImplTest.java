package com.nivasafinance.features.master.codemaster.service.impl;

import com.nivasafinance.common.base.model.MasterLanguageData;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.features.master.codemaster.dto.*;
import com.nivasafinance.features.master.codemaster.entity.MasterCode;
import com.nivasafinance.features.master.codemaster.entity.MasterCodeValue;
import com.nivasafinance.features.master.codemaster.repository.MasterCodeRepositoryWrapper;
import com.nivasafinance.features.master.codemaster.repository.MasterCodeValueRepositoryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CodeMasterServiceImplTest {

    @Mock
    private MasterCodeRepositoryWrapper masterCodeRepositoryWrapper;

    @Mock
    private MasterCodeValueRepositoryWrapper masterCodeValueRepositoryWrapper;

    @InjectMocks
    private CodeMasterServiceImpl codeMasterService;

    private static final String TEST_CODE_KEY = "LOAN_TYPE";
    private static final String TEST_PARENT_CODE_KEY = "PARENT_KEY";
    private static final Long TEST_PARENT_ID = 1L;

    private MasterCode masterCode;
    private MasterCode parentMasterCode;
    private MasterCodeValue masterCodeValue;
    private PaginationRequest paginationRequest;

    @BeforeEach
    void setUp() {
        paginationRequest = new PaginationRequest(0, 20, "createdAt", "DESC");

        masterCode = MasterCode.builder()
                .id(1L)
                .key(TEST_CODE_KEY)
                .name(MasterLanguageData.builder().defaultValue("Loan Type").build())
                .description(MasterLanguageData.builder().defaultValue("Types of loans").build())
                .isSystemDefined(false)
                .parentId(null)
                .build();

        parentMasterCode = MasterCode.builder()
                .id(TEST_PARENT_ID)
                .key(TEST_PARENT_CODE_KEY)
                .name(MasterLanguageData.builder().defaultValue("Parent").build())
                .description(MasterLanguageData.builder().defaultValue("Parent desc").build())
                .isSystemDefined(false)
                .parentId(null)
                .build();

        masterCodeValue = MasterCodeValue.builder()
                .id(10L)
                .key("HOME_LOAN_MASTER_CODE_VALUE")
                .codeKey(TEST_CODE_KEY)
                .value(MasterLanguageData.builder().defaultValue("Home Loan").build())
                .description(MasterLanguageData.builder().defaultValue("Home loan desc").build())
                .isActive(true)
                .displayOrder(0)
                .build();
    }

    // ── getAllCodeValuesByCodeKey ─────────────────────────────────────

    @Test
    void getAllCodeValuesByCodeKey_onlyActiveTrue_returnsActiveValues() {
        when(masterCodeRepositoryWrapper.findByKeyWithException(TEST_CODE_KEY)).thenReturn(masterCode);
        when(masterCodeValueRepositoryWrapper.findByCodeKeyAndIsActiveTrueWithException(TEST_CODE_KEY))
                .thenReturn(List.of(masterCodeValue));

        List<CodeValueResponse> result = codeMasterService.getAllCodeValuesByCodeKey(TEST_CODE_KEY, true, null);

        assertEquals(1, result.size(), "Should return exactly one active code value");
        assertEquals("HOME_LOAN_MASTER_CODE_VALUE", result.get(0).getKey(),
                "Returned code value key should match the mocked value");
        verify(masterCodeValueRepositoryWrapper).findByCodeKeyAndIsActiveTrueWithException(TEST_CODE_KEY);
        verify(masterCodeValueRepositoryWrapper, never()).findByCodeKeyWithException(TEST_CODE_KEY);
    }

    @Test
    void getAllCodeValuesByCodeKey_onlyActiveFalse_returnsAllValues() {
        when(masterCodeRepositoryWrapper.findByKeyWithException(TEST_CODE_KEY)).thenReturn(masterCode);
        when(masterCodeValueRepositoryWrapper.findByCodeKeyWithException(TEST_CODE_KEY))
                .thenReturn(List.of(masterCodeValue));

        List<CodeValueResponse> result = codeMasterService.getAllCodeValuesByCodeKey(TEST_CODE_KEY, false, null);

        assertEquals(1, result.size(), "Should return all code values regardless of active state");
        verify(masterCodeValueRepositoryWrapper).findByCodeKeyWithException(TEST_CODE_KEY);
        verify(masterCodeValueRepositoryWrapper, never()).findByCodeKeyAndIsActiveTrueWithException(anyString());
    }

    @Test
    void getAllCodeValuesByCodeKey_withValidContext_returnsContextFilteredIcons() {
        when(masterCodeRepositoryWrapper.findByKeyWithException(TEST_CODE_KEY)).thenReturn(masterCode);
        when(masterCodeValueRepositoryWrapper.findByCodeKeyAndIsActiveTrueWithException(TEST_CODE_KEY))
                .thenReturn(List.of(masterCodeValue));

        List<CodeValueResponse> result = codeMasterService.getAllCodeValuesByCodeKey(TEST_CODE_KEY, true, "crm");

        assertNotNull(result, "Result should not be null when context is provided");
        assertEquals(1, result.size(), "Should still return one code value with context-filtered icons");
    }

    @Test
    void getAllCodeValuesByCodeKey_withInvalidContext_throwsIllegalArgument() {
        when(masterCodeRepositoryWrapper.findByKeyWithException(TEST_CODE_KEY)).thenReturn(masterCode);
        when(masterCodeValueRepositoryWrapper.findByCodeKeyAndIsActiveTrueWithException(TEST_CODE_KEY))
                .thenReturn(List.of(masterCodeValue));

        assertThrows(IllegalArgumentException.class,
                () -> codeMasterService.getAllCodeValuesByCodeKey(TEST_CODE_KEY, true, "INVALID"),
                "Invalid context string should throw IllegalArgumentException");
    }

    @Test
    void getAllCodeValuesByCodeKey_emptyValueList_returnsEmptyList() {
        when(masterCodeRepositoryWrapper.findByKeyWithException(TEST_CODE_KEY)).thenReturn(masterCode);
        when(masterCodeValueRepositoryWrapper.findByCodeKeyAndIsActiveTrueWithException(TEST_CODE_KEY))
                .thenReturn(Collections.emptyList());

        List<CodeValueResponse> result = codeMasterService.getAllCodeValuesByCodeKey(TEST_CODE_KEY, true, null);

        assertTrue(result.isEmpty(), "Should return empty list when no code values exist");
    }

    // ── getCodeValuesByCodeKeyPaginated ──────────────────────────────

    @Test
    void getCodeValuesByCodeKeyPaginated_happyPath_returnsPaginatedValues() {
        PaginationInfo pageInfo = new PaginationInfo(0, 20, 1, 1, 0, false, false);
        PaginatedResponse<MasterCodeValue> paginatedValues = new PaginatedResponse<>(
                List.of(masterCodeValue), pageInfo);

        when(masterCodeRepositoryWrapper.findByKeyWithException(TEST_CODE_KEY)).thenReturn(masterCode);
        when(masterCodeValueRepositoryWrapper.findByCodeKeyWithException(TEST_CODE_KEY, true, paginationRequest))
                .thenReturn(paginatedValues);

        PaginatedResponse<CodeValueResponse> result = codeMasterService.getCodeValuesByCodeKeyPaginated(
                TEST_CODE_KEY, true, null, paginationRequest);

        assertNotNull(result, "Paginated response should not be null");
        assertEquals(1, result.getContent().size(), "Should contain one code value in paginated result");
        assertEquals(pageInfo, result.getPagination(), "Pagination info should be passed through unchanged");
    }

    @Test
    void getCodeValuesByCodeKeyPaginated_withContext_appliesContextFilter() {
        PaginationInfo pageInfo = new PaginationInfo(0, 20, 1, 1, 0, false, false);
        PaginatedResponse<MasterCodeValue> paginatedValues = new PaginatedResponse<>(
                List.of(masterCodeValue), pageInfo);

        when(masterCodeRepositoryWrapper.findByKeyWithException(TEST_CODE_KEY)).thenReturn(masterCode);
        when(masterCodeValueRepositoryWrapper.findByCodeKeyWithException(TEST_CODE_KEY, true, paginationRequest))
                .thenReturn(paginatedValues);

        PaginatedResponse<CodeValueResponse> result = codeMasterService.getCodeValuesByCodeKeyPaginated(
                TEST_CODE_KEY, true, "web", paginationRequest);

        assertNotNull(result, "Paginated response should not be null when context is specified");
        assertEquals(1, result.getContent().size(), "Should return values with context-specific icon filtering");
    }

    // ── getMasterCodeChildrenWithValues ───────────────────────────────

    @Test
    void getMasterCodeChildrenWithValues_happyPath_returnsChildrenWithValues() {
        MasterCode child = MasterCode.builder()
                .id(2L).key("CHILD_KEY")
                .name(MasterLanguageData.builder().defaultValue("Child").build())
                .description(MasterLanguageData.builder().defaultValue("Child desc").build())
                .isSystemDefined(false).parentId(TEST_PARENT_ID).build();

        when(masterCodeRepositoryWrapper.findByKeyWithException(TEST_PARENT_CODE_KEY)).thenReturn(parentMasterCode);
        when(masterCodeRepositoryWrapper.findByParentIdWithException(TEST_PARENT_ID)).thenReturn(List.of(child));
        when(masterCodeValueRepositoryWrapper.findByCodeKeyAndIsActiveTrueWithException("CHILD_KEY"))
                .thenReturn(List.of(masterCodeValue));

        List<MasterCodeWithValuesResponse> result = codeMasterService.getMasterCodeChildrenWithValues(
                TEST_PARENT_CODE_KEY, true, null);

        assertEquals(1, result.size(), "Should return one child master code");
        assertEquals("CHILD_KEY", result.get(0).getKey(), "Child key should match the mocked child");
        assertFalse(result.get(0).getValues().isEmpty(), "Child should have associated values");
    }

    @Test
    void getMasterCodeChildrenWithValues_parentIdNull_throwsIllegalState() {
        MasterCode parentWithNullId = MasterCode.builder()
                .id(null).key(TEST_PARENT_CODE_KEY).build();

        when(masterCodeRepositoryWrapper.findByKeyWithException(TEST_PARENT_CODE_KEY)).thenReturn(parentWithNullId);

        assertThrows(IllegalStateException.class,
                () -> codeMasterService.getMasterCodeChildrenWithValues(TEST_PARENT_CODE_KEY, true, null),
                "Should throw IllegalStateException when parent master code ID is null");
    }

    @Test
    void getMasterCodeChildrenWithValues_onlyActiveFalse_fetchesAllValues() {
        MasterCode child = MasterCode.builder()
                .id(2L).key("CHILD_KEY")
                .name(MasterLanguageData.builder().defaultValue("Child").build())
                .description(MasterLanguageData.builder().defaultValue("Child desc").build())
                .isSystemDefined(false).parentId(TEST_PARENT_ID).build();

        when(masterCodeRepositoryWrapper.findByKeyWithException(TEST_PARENT_CODE_KEY)).thenReturn(parentMasterCode);
        when(masterCodeRepositoryWrapper.findByParentIdWithException(TEST_PARENT_ID)).thenReturn(List.of(child));
        when(masterCodeValueRepositoryWrapper.findByCodeKeyWithException("CHILD_KEY"))
                .thenReturn(List.of(masterCodeValue));

        List<MasterCodeWithValuesResponse> result = codeMasterService.getMasterCodeChildrenWithValues(
                TEST_PARENT_CODE_KEY, false, null);

        assertEquals(1, result.size(), "Should return children when onlyActive is false");
        verify(masterCodeValueRepositoryWrapper).findByCodeKeyWithException("CHILD_KEY");
        verify(masterCodeValueRepositoryWrapper, never()).findByCodeKeyAndIsActiveTrueWithException(anyString());
    }

    @Test
    void getMasterCodeChildrenWithValues_noChildren_returnsEmptyList() {
        when(masterCodeRepositoryWrapper.findByKeyWithException(TEST_PARENT_CODE_KEY)).thenReturn(parentMasterCode);
        when(masterCodeRepositoryWrapper.findByParentIdWithException(TEST_PARENT_ID))
                .thenReturn(Collections.emptyList());

        List<MasterCodeWithValuesResponse> result = codeMasterService.getMasterCodeChildrenWithValues(
                TEST_PARENT_CODE_KEY, true, null);

        assertTrue(result.isEmpty(), "Should return empty list when parent has no children");
    }

    // ── getMasterCodeChildrenWithValuesPaginated ─────────────────────

    @Test
    void getMasterCodeChildrenWithValuesPaginated_happyPath_returnsPaginatedChildren() {
        MasterCode child = MasterCode.builder()
                .id(2L).key("CHILD_KEY")
                .name(MasterLanguageData.builder().defaultValue("Child").build())
                .description(MasterLanguageData.builder().defaultValue("Child desc").build())
                .isSystemDefined(false).parentId(TEST_PARENT_ID).build();

        PaginationInfo pageInfo = new PaginationInfo(0, 20, 1, 1, 0, false, false);
        PaginatedResponse<MasterCode> paginatedChildren = new PaginatedResponse<>(List.of(child), pageInfo);

        when(masterCodeRepositoryWrapper.findByKeyWithException(TEST_PARENT_CODE_KEY)).thenReturn(parentMasterCode);
        when(masterCodeRepositoryWrapper.findByParentIdWithException(TEST_PARENT_ID, paginationRequest))
                .thenReturn(paginatedChildren);
        when(masterCodeValueRepositoryWrapper.findByCodeKeyAndIsActiveTrueWithException("CHILD_KEY"))
                .thenReturn(List.of(masterCodeValue));

        PaginatedResponse<MasterCodeWithValuesResponse> result =
                codeMasterService.getMasterCodeChildrenWithValuesPaginated(
                        TEST_PARENT_CODE_KEY, true, null, paginationRequest);

        assertNotNull(result, "Paginated response should not be null");
        assertEquals(1, result.getContent().size(), "Should contain one child in paginated result");
    }

    @Test
    void getMasterCodeChildrenWithValuesPaginated_parentIdNull_throwsIllegalState() {
        MasterCode parentWithNullId = MasterCode.builder()
                .id(null).key(TEST_PARENT_CODE_KEY).build();

        when(masterCodeRepositoryWrapper.findByKeyWithException(TEST_PARENT_CODE_KEY)).thenReturn(parentWithNullId);

        assertThrows(IllegalStateException.class,
                () -> codeMasterService.getMasterCodeChildrenWithValuesPaginated(
                        TEST_PARENT_CODE_KEY, true, null, paginationRequest),
                "Should throw IllegalStateException when parent master code ID is null");
    }

    // ── getMasterCodeByKey ───────────────────────────────────────────

    @Test
    void getMasterCodeByKey_existingKey_returnsMappedResponse() {
        when(masterCodeRepositoryWrapper.findByKeyWithException(TEST_CODE_KEY)).thenReturn(masterCode);

        MasterCodeResponse result = codeMasterService.getMasterCodeByKey(TEST_CODE_KEY);

        assertNotNull(result, "Response should not be null for an existing key");
        assertEquals(TEST_CODE_KEY, result.getKey(), "Returned key should match the requested key");
        verify(masterCodeRepositoryWrapper).findByKeyWithException(TEST_CODE_KEY);
    }

    // ── getMasterCodesByKeys ─────────────────────────────────────────

    @Test
    void getMasterCodesByKeys_multipleKeys_returnsAllMapped() {
        MasterCode secondCode = MasterCode.builder()
                .id(2L).key("SECOND_KEY")
                .name(MasterLanguageData.builder().defaultValue("Second").build())
                .description(MasterLanguageData.builder().defaultValue("Second desc").build())
                .isSystemDefined(false).build();

        when(masterCodeRepositoryWrapper.findByKeyWithException(TEST_CODE_KEY)).thenReturn(masterCode);
        when(masterCodeRepositoryWrapper.findByKeyWithException("SECOND_KEY")).thenReturn(secondCode);

        List<MasterCodeResponse> result = codeMasterService.getMasterCodesByKeys(
                List.of(TEST_CODE_KEY, "SECOND_KEY"));

        assertEquals(2, result.size(), "Should return a response for each requested key");
        assertEquals(TEST_CODE_KEY, result.get(0).getKey(), "First response should match first key");
        assertEquals("SECOND_KEY", result.get(1).getKey(), "Second response should match second key");
    }

    // ── getAllMasterCodes ────────────────────────────────────────────

    @Test
    void getAllMasterCodes_happyPath_returnsPaginatedMasterCodes() {
        PaginationInfo pageInfo = new PaginationInfo(0, 20, 1, 1, 0, false, false);
        PaginatedResponse<MasterCode> paginatedCodes = new PaginatedResponse<>(List.of(masterCode), pageInfo);

        when(masterCodeRepositoryWrapper.findAllWithException(paginationRequest)).thenReturn(paginatedCodes);

        PaginatedResponse<MasterCodeResponse> result = codeMasterService.getAllMasterCodes(paginationRequest);

        assertNotNull(result, "Paginated response should not be null");
        assertEquals(1, result.getContent().size(), "Should contain one master code");
        assertEquals(TEST_CODE_KEY, result.getContent().get(0).getKey(),
                "Master code key in response should match the entity key");
    }

    // ── updateMasterCodeWithValues ───────────────────────────────────

    @Test
    void updateMasterCodeWithValues_updateName_appliesNamePatch() {
        MasterCodeWithValuesRequest request = MasterCodeWithValuesRequest.builder()
                .nameMap(Map.of("default", "Updated Name"))
                .descriptionMap(Map.of("other", "no-default"))
                .build();

        when(masterCodeRepositoryWrapper.findByKeyWithException(TEST_CODE_KEY)).thenReturn(masterCode);
        when(masterCodeRepositoryWrapper.saveWithException(masterCode)).thenReturn(masterCode);
        when(masterCodeValueRepositoryWrapper.findByCodeKeyWithException(TEST_CODE_KEY))
                .thenReturn(List.of(masterCodeValue));

        MasterCodeValueResponse result = codeMasterService.updateMasterCodeWithValues(TEST_CODE_KEY, request);

        assertNotNull(result, "Update response should not be null");
        assertEquals("Updated Name", masterCode.getName().getDefaultValue(),
                "Master code name should be updated to the new value");
        verify(masterCodeRepositoryWrapper).saveWithException(masterCode);
    }

    @Test
    void updateMasterCodeWithValues_updateDescription_appliesDescriptionPatch() {
        MasterCodeWithValuesRequest request = MasterCodeWithValuesRequest.builder()
                .nameMap(Map.of("other", "no-default"))
                .descriptionMap(Map.of("default", "Updated Desc"))
                .build();

        when(masterCodeRepositoryWrapper.findByKeyWithException(TEST_CODE_KEY)).thenReturn(masterCode);
        when(masterCodeRepositoryWrapper.saveWithException(masterCode)).thenReturn(masterCode);
        when(masterCodeValueRepositoryWrapper.findByCodeKeyWithException(TEST_CODE_KEY))
                .thenReturn(List.of(masterCodeValue));

        codeMasterService.updateMasterCodeWithValues(TEST_CODE_KEY, request);

        assertEquals("Updated Desc", masterCode.getDescription().getDefaultValue(),
                "Master code description should be updated to the new value");
    }

    @Test
    void updateMasterCodeWithValues_updateParentId_appliesParentIdPatch() {
        MasterCodeWithValuesRequest request = MasterCodeWithValuesRequest.builder()
                .nameMap(Map.of("other", "no-default"))
                .descriptionMap(Map.of("other", "no-default"))
                .parentId(99L)
                .build();

        when(masterCodeRepositoryWrapper.findByKeyWithException(TEST_CODE_KEY)).thenReturn(masterCode);
        when(masterCodeRepositoryWrapper.saveWithException(masterCode)).thenReturn(masterCode);
        when(masterCodeValueRepositoryWrapper.findByCodeKeyWithException(TEST_CODE_KEY))
                .thenReturn(List.of(masterCodeValue));

        codeMasterService.updateMasterCodeWithValues(TEST_CODE_KEY, request);

        assertEquals(99L, masterCode.getParentId(),
                "Parent ID should be updated to the new value");
    }

    @Test
    void updateMasterCodeWithValues_withValuePatch_updatesCodeValueFields() {
        MasterCodeValueRequest valueRequest = MasterCodeValueRequest.builder()
                .key("HOME_LOAN_MASTER_CODE_VALUE")
                .valueMap(Map.of("default", "Updated Home Loan"))
                .descriptionMap(Map.of("default", "Updated desc"))
                .build();

        MasterCodeWithValuesRequest request = MasterCodeWithValuesRequest.builder()
                .nameMap(Map.of("other", "no-default"))
                .descriptionMap(Map.of("other", "no-default"))
                .masterCodeValueRequests(List.of(valueRequest))
                .build();

        when(masterCodeRepositoryWrapper.findByKeyWithException(TEST_CODE_KEY)).thenReturn(masterCode);
        when(masterCodeRepositoryWrapper.saveWithException(masterCode)).thenReturn(masterCode);
        when(masterCodeValueRepositoryWrapper.findByKeyWithException("HOME_LOAN_MASTER_CODE_VALUE"))
                .thenReturn(masterCodeValue);
        when(masterCodeValueRepositoryWrapper.findByCodeKeyWithException(TEST_CODE_KEY))
                .thenReturn(List.of(masterCodeValue));

        codeMasterService.updateMasterCodeWithValues(TEST_CODE_KEY, request);

        assertEquals("Updated Home Loan", masterCodeValue.getValue().getDefaultValue(),
                "Code value should be updated with the new value from the request");
        verify(masterCodeValueRepositoryWrapper).saveWithException(masterCodeValue);
    }

    @Test
    void updateMasterCodeWithValues_emptyValueRequests_skipsValuePatch() {
        MasterCodeWithValuesRequest request = MasterCodeWithValuesRequest.builder()
                .nameMap(Map.of("default", "Name"))
                .descriptionMap(Map.of("other", "no-default"))
                .masterCodeValueRequests(Collections.emptyList())
                .build();

        when(masterCodeRepositoryWrapper.findByKeyWithException(TEST_CODE_KEY)).thenReturn(masterCode);
        when(masterCodeRepositoryWrapper.saveWithException(masterCode)).thenReturn(masterCode);
        when(masterCodeValueRepositoryWrapper.findByCodeKeyWithException(TEST_CODE_KEY))
                .thenReturn(List.of(masterCodeValue));

        MasterCodeValueResponse result = codeMasterService.updateMasterCodeWithValues(TEST_CODE_KEY, request);

        assertNotNull(result, "Response should not be null even with empty value requests");
        verify(masterCodeValueRepositoryWrapper, never()).findByKeyWithException(anyString());
    }

    @Test
    void updateMasterCodeWithValues_nullValueRequests_skipsValuePatch() {
        MasterCodeWithValuesRequest request = MasterCodeWithValuesRequest.builder()
                .nameMap(Map.of("default", "Name"))
                .descriptionMap(Map.of("other", "no-default"))
                .masterCodeValueRequests(null)
                .build();

        when(masterCodeRepositoryWrapper.findByKeyWithException(TEST_CODE_KEY)).thenReturn(masterCode);
        when(masterCodeRepositoryWrapper.saveWithException(masterCode)).thenReturn(masterCode);
        when(masterCodeValueRepositoryWrapper.findByCodeKeyWithException(TEST_CODE_KEY))
                .thenReturn(List.of(masterCodeValue));

        MasterCodeValueResponse result = codeMasterService.updateMasterCodeWithValues(TEST_CODE_KEY, request);

        assertNotNull(result, "Response should not be null even with null value requests");
        verify(masterCodeValueRepositoryWrapper, never()).findByKeyWithException(anyString());
    }

    // ── getMasterCodeTree ────────────────────────────────────────────

    @Test
    void getMasterCodeTree_noChildren_returnsSingleNodeTree() {
        when(masterCodeRepositoryWrapper.findByKeyWithException(TEST_PARENT_CODE_KEY)).thenReturn(parentMasterCode);
        when(masterCodeRepositoryWrapper.findByParentIdWithException(TEST_PARENT_ID))
                .thenReturn(Collections.emptyList());

        List<MasterCodeTreeResponse> result = codeMasterService.getMasterCodeTree(TEST_PARENT_CODE_KEY);

        assertEquals(1, result.size(), "Tree should contain exactly one root node");
        assertTrue(result.get(0).getChildren().isEmpty(), "Root node should have no children");
    }

    @Test
    void getMasterCodeTree_withChildren_buildsRecursiveTree() {
        MasterCode child = MasterCode.builder()
                .id(2L).key("CHILD_KEY")
                .name(MasterLanguageData.builder().defaultValue("Child").build())
                .description(MasterLanguageData.builder().defaultValue("Child desc").build())
                .isSystemDefined(false).parentId(TEST_PARENT_ID).build();

        when(masterCodeRepositoryWrapper.findByKeyWithException(TEST_PARENT_CODE_KEY)).thenReturn(parentMasterCode);
        when(masterCodeRepositoryWrapper.findByParentIdWithException(TEST_PARENT_ID)).thenReturn(List.of(child));
        when(masterCodeRepositoryWrapper.findByParentIdWithException(2L)).thenReturn(Collections.emptyList());

        List<MasterCodeTreeResponse> result = codeMasterService.getMasterCodeTree(TEST_PARENT_CODE_KEY);

        assertEquals(1, result.size(), "Tree should have one root node");
        assertEquals(1, result.get(0).getChildren().size(), "Root should have one child");
        assertEquals("CHILD_KEY", result.get(0).getChildren().get(0).getKey(),
                "Child key in tree should match the mocked child");
    }

    // ── addChildToTree ───────────────────────────────────────────────

    @Test
    void addChildToTree_withValues_savesChildAndValues() {
        MasterCodeValueRequest valueReq = MasterCodeValueRequest.builder()
                .valueMap(Map.of("default", "Val1"))
                .descriptionMap(Map.of("default", "Desc1"))
                .build();

        MasterCodeWithValuesRequest childRequest = MasterCodeWithValuesRequest.builder()
                .nameMap(Map.of("default", "Child Name"))
                .descriptionMap(Map.of("default", "Child Desc"))
                .masterCodeValueRequests(List.of(valueReq))
                .build();

        when(masterCodeRepositoryWrapper.findByKeyWithException(TEST_PARENT_CODE_KEY)).thenReturn(parentMasterCode);
        when(masterCodeRepositoryWrapper.findAllWithException()).thenReturn(List.of(parentMasterCode));
        when(masterCodeRepositoryWrapper.saveWithException(any(MasterCode.class))).thenAnswer(inv -> inv.getArgument(0));
        when(masterCodeValueRepositoryWrapper.saveWithException(any(MasterCodeValue.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(masterCodeRepositoryWrapper.findByParentIdWithException(TEST_PARENT_ID))
                .thenReturn(Collections.emptyList());

        List<MasterCodeTreeResponse> result = codeMasterService.addChildToTree(TEST_PARENT_CODE_KEY, childRequest);

        assertNotNull(result, "Tree response should not be null after adding child");
        verify(masterCodeRepositoryWrapper).saveWithException(any(MasterCode.class));
        verify(masterCodeValueRepositoryWrapper).saveWithException(any(MasterCodeValue.class));
    }

    @Test
    void addChildToTree_withoutValues_savesChildOnly() {
        MasterCodeWithValuesRequest childRequest = MasterCodeWithValuesRequest.builder()
                .nameMap(Map.of("default", "Child"))
                .descriptionMap(Map.of("default", "Desc"))
                .masterCodeValueRequests(null)
                .build();

        when(masterCodeRepositoryWrapper.findByKeyWithException(TEST_PARENT_CODE_KEY)).thenReturn(parentMasterCode);
        when(masterCodeRepositoryWrapper.findAllWithException()).thenReturn(List.of(parentMasterCode));
        when(masterCodeRepositoryWrapper.saveWithException(any(MasterCode.class))).thenAnswer(inv -> inv.getArgument(0));
        when(masterCodeRepositoryWrapper.findByParentIdWithException(TEST_PARENT_ID))
                .thenReturn(Collections.emptyList());

        List<MasterCodeTreeResponse> result = codeMasterService.addChildToTree(TEST_PARENT_CODE_KEY, childRequest);

        assertNotNull(result, "Tree response should not be null after adding child without values");
        verify(masterCodeRepositoryWrapper).saveWithException(any(MasterCode.class));
        verify(masterCodeValueRepositoryWrapper, never()).saveWithException(any(MasterCodeValue.class));
    }

    // ── searchMasterCodes ────────────────────────────────────────────

    @Test
    void searchMasterCodes_masterContext_searchesMasterCodesOnly() {
        PaginationInfo pageInfo = new PaginationInfo(0, 20, 0, 0, 0, false, false);
        PaginatedResponse<MasterCodeSearchResponse> masterResults = new PaginatedResponse<>(
                Collections.emptyList(), pageInfo);

        when(masterCodeRepositoryWrapper.searchMasterCodesOnly("tes", paginationRequest)).thenReturn(masterResults);

        MasterCodeSearchMultiSectionResponse result = codeMasterService.searchMasterCodes(
                "tes", List.of(SearchContext.MASTER), null, paginationRequest);

        assertNotNull(result.getMasterMatches(), "Master matches should be populated for MASTER context");
        assertNull(result.getChildMatches(), "Child matches should be null when CHILD context not requested");
        assertNull(result.getValueMatches(), "Value matches should be null when VALUE context not requested");
    }

    @Test
    void searchMasterCodes_childContext_searchesChildCodesOnly() {
        PaginationInfo pageInfo = new PaginationInfo(0, 20, 0, 0, 0, false, false);
        PaginatedResponse<MasterCodeSearchResponse> childResults = new PaginatedResponse<>(
                Collections.emptyList(), pageInfo);

        when(masterCodeRepositoryWrapper.searchChildCodesOnly("tes", paginationRequest)).thenReturn(childResults);

        MasterCodeSearchMultiSectionResponse result = codeMasterService.searchMasterCodes(
                "tes", List.of(SearchContext.CHILD), null, paginationRequest);

        assertNotNull(result.getChildMatches(), "Child matches should be populated for CHILD context");
        assertNull(result.getMasterMatches(), "Master matches should be null when MASTER context not requested");
    }

    @Test
    void searchMasterCodes_valueContextWithCodeKey_searchesValues() {
        PaginationInfo pageInfo = new PaginationInfo(0, 20, 0, 0, 0, false, false);
        PaginatedResponse<MasterCodeSearchResponse> valueResults = new PaginatedResponse<>(
                Collections.emptyList(), pageInfo);

        when(masterCodeValueRepositoryWrapper.searchMasterCodeValues("tes", TEST_CODE_KEY, paginationRequest))
                .thenReturn(valueResults);

        MasterCodeSearchMultiSectionResponse result = codeMasterService.searchMasterCodes(
                "tes", List.of(SearchContext.VALUE), TEST_CODE_KEY, paginationRequest);

        assertNotNull(result.getValueMatches(), "Value matches should be populated for VALUE context with codeKey");
    }

    @Test
    void searchMasterCodes_valueContextWithoutCodeKey_throwsBadRequest() {
        assertThrows(BadRequestException.class,
                () -> codeMasterService.searchMasterCodes(
                        "tes", List.of(SearchContext.VALUE), null, paginationRequest),
                "VALUE context without codeKey should throw BadRequestException");
    }

    @Test
    void searchMasterCodes_valueContextWithBlankCodeKey_throwsBadRequest() {
        assertThrows(BadRequestException.class,
                () -> codeMasterService.searchMasterCodes(
                        "tes", List.of(SearchContext.VALUE), "  ", paginationRequest),
                "VALUE context with blank codeKey should throw BadRequestException");
    }

    @Test
    void searchMasterCodes_blankSearchTerm_throwsBadRequest() {
        assertThrows(BadRequestException.class,
                () -> codeMasterService.searchMasterCodes(
                        "  ", List.of(SearchContext.MASTER), null, paginationRequest),
                "Blank search term should throw BadRequestException");
    }

    @Test
    void searchMasterCodes_nullSearchTerm_throwsBadRequest() {
        assertThrows(BadRequestException.class,
                () -> codeMasterService.searchMasterCodes(
                        null, List.of(SearchContext.MASTER), null, paginationRequest),
                "Null search term should throw BadRequestException");
    }

    @Test
    void searchMasterCodes_shortSearchTerm_throwsBadRequest() {
        assertThrows(BadRequestException.class,
                () -> codeMasterService.searchMasterCodes(
                        "ab", List.of(SearchContext.MASTER), null, paginationRequest),
                "Search term shorter than minimum length should throw BadRequestException");
    }

    @Test
    void searchMasterCodes_duplicateContexts_deduplicatedBeforeSearch() {
        PaginationInfo pageInfo = new PaginationInfo(0, 20, 0, 0, 0, false, false);
        PaginatedResponse<MasterCodeSearchResponse> masterResults = new PaginatedResponse<>(
                Collections.emptyList(), pageInfo);

        when(masterCodeRepositoryWrapper.searchMasterCodesOnly("test", paginationRequest)).thenReturn(masterResults);

        MasterCodeSearchMultiSectionResponse result = codeMasterService.searchMasterCodes(
                "test", List.of(SearchContext.MASTER, SearchContext.MASTER), null, paginationRequest);

        assertNotNull(result.getMasterMatches(), "Master matches should be populated even with duplicated contexts");
        verify(masterCodeRepositoryWrapper, times(1)).searchMasterCodesOnly("test", paginationRequest);
    }

    @Test
    void searchMasterCodes_multipleDistinctContexts_searchesAll() {
        PaginationInfo pageInfo = new PaginationInfo(0, 20, 0, 0, 0, false, false);
        PaginatedResponse<MasterCodeSearchResponse> results = new PaginatedResponse<>(
                Collections.emptyList(), pageInfo);

        when(masterCodeRepositoryWrapper.searchMasterCodesOnly("test", paginationRequest)).thenReturn(results);
        when(masterCodeRepositoryWrapper.searchChildCodesOnly("test", paginationRequest)).thenReturn(results);
        when(masterCodeValueRepositoryWrapper.searchMasterCodeValues("test", TEST_CODE_KEY, paginationRequest))
                .thenReturn(results);

        MasterCodeSearchMultiSectionResponse result = codeMasterService.searchMasterCodes(
                "test", List.of(SearchContext.MASTER, SearchContext.CHILD, SearchContext.VALUE),
                TEST_CODE_KEY, paginationRequest);

        assertNotNull(result.getMasterMatches(), "Master matches should be populated");
        assertNotNull(result.getChildMatches(), "Child matches should be populated");
        assertNotNull(result.getValueMatches(), "Value matches should be populated");
    }
}
