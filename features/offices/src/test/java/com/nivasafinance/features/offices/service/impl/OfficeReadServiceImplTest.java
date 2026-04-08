package com.nivasafinance.features.offices.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.offices.dto.OfficeResponse;
import com.nivasafinance.features.offices.dto.OfficeTreeNodeResponse;
import com.nivasafinance.features.offices.entity.Office;
import com.nivasafinance.features.offices.exception.OfficeNotFoundException;
import com.nivasafinance.features.offices.repository.OfficeRepository;
import com.nivasafinance.features.offices.repository.OfficeRepositoryWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfficeReadServiceImplTest {

    @Mock
    private OfficeRepository officeRepository;

    @Mock
    private OfficeRepositoryWrapper officeRepositoryWrapper;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private OfficeReadServiceImpl officeReadService;

    private Office buildOffice(Long id, String name, String key, String code, Long parentId) {
        Office o = new Office();
        o.setId(id);
        o.setName(name);
        o.setKey(key);
        o.setCode(code);
        o.setParentId(parentId);
        o.setIsActive(true);
        return o;
    }

    // --- getOfficeByKey ---

    @Test
    void getOfficeByKey_validKey_returnsResponse() {
        Office office = buildOffice(1L, "HQ", "hq-key", "001", null);
        when(officeRepository.findByKey("hq-key")).thenReturn(Optional.of(office));

        OfficeResponse result = officeReadService.getOfficeByKey("hq-key");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("HQ", result.getName());
        assertEquals("hq-key", result.getKey());
        assertEquals("001", result.getCode());
        assertNull(result.getParentId());
        assertTrue(result.getIsActive());
    }

    @Test
    void getOfficeByKey_keyNotFound_throwsOfficeNotFoundException() {
        when(officeRepository.findByKey("missing")).thenReturn(Optional.empty());

        assertThrows(OfficeNotFoundException.class, () -> officeReadService.getOfficeByKey("missing"));
    }

    // --- getOfficeByKeys ---

    @Test
    void getOfficeByKeys_multipleKeys_returnsAll() {
        Office o1 = buildOffice(1L, "A", "key-a", "001", null);
        Office o2 = buildOffice(2L, "B", "key-b", "002", null);
        Office o3 = buildOffice(3L, "C", "key-c", "003", null);
        when(officeRepository.findByKey("key-a")).thenReturn(Optional.of(o1));
        when(officeRepository.findByKey("key-b")).thenReturn(Optional.of(o2));
        when(officeRepository.findByKey("key-c")).thenReturn(Optional.of(o3));

        List<OfficeResponse> result = officeReadService.getOfficeByKeys(List.of("key-a", "key-b", "key-c"));

        assertEquals(3, result.size());
        assertEquals("A", result.get(0).getName());
        assertEquals("B", result.get(1).getName());
        assertEquals("C", result.get(2).getName());
    }

    @Test
    void getOfficeByKeys_emptyList_returnsEmpty() {
        List<OfficeResponse> result = officeReadService.getOfficeByKeys(List.of());

        assertTrue(result.isEmpty());
    }

    @Test
    void getOfficeByKeys_oneKeyNotFound_throwsException() {
        Office o1 = buildOffice(1L, "A", "key-a", "001", null);
        when(officeRepository.findByKey("key-a")).thenReturn(Optional.of(o1));
        when(officeRepository.findByKey("key-missing")).thenReturn(Optional.empty());

        assertThrows(OfficeNotFoundException.class,
                () -> officeReadService.getOfficeByKeys(List.of("key-a", "key-missing")));
    }

    // --- getOffices ---

    @Test
    void getOffices_withParentKey_looksUpParent() {
        Office parent = buildOffice(1L, "Parent", "parent-key", "001", null);
        when(officeRepository.findByKey("parent-key")).thenReturn(Optional.of(parent));

        PaginationInfo pagination = new PaginationInfo(0, 10, 1L, 1, 0, false, false);
        Office child = buildOffice(2L, "Child", "child-key", "001.001", 1L);
        PaginatedResponse<Office> paginatedOffices = new PaginatedResponse<>(List.of(child), pagination);
        when(officeRepositoryWrapper.findOffices(eq("001"), isNull(), isNull(), any(PaginationRequest.class)))
                .thenReturn(paginatedOffices);

        PaginationRequest request = new PaginationRequest(0, 10, "name", "ASC");
        PaginatedResponse<OfficeResponse> result = officeReadService.getOffices("parent-key", null, null, request);

        assertEquals(1, result.getContent().size());
        assertEquals("Child", result.getContent().get(0).getName());
        verify(officeRepository).findByKey("parent-key");
    }

    @Test
    void getOffices_withoutParentKey_nullPrefix() {
        PaginationInfo pagination = new PaginationInfo(0, 10, 0L, 0, 0, false, false);
        PaginatedResponse<Office> paginatedOffices = new PaginatedResponse<>(List.of(), pagination);
        when(officeRepositoryWrapper.findOffices(isNull(), isNull(), isNull(), any(PaginationRequest.class)))
                .thenReturn(paginatedOffices);

        PaginationRequest request = new PaginationRequest(0, 10, "name", "ASC");
        PaginatedResponse<OfficeResponse> result = officeReadService.getOffices(null, null, null, request);

        assertTrue(result.getContent().isEmpty());
        verify(officeRepository, never()).findByKey(anyString());
    }

    @Test
    void getOffices_parentNotFound_throwsException() {
        when(officeRepository.findByKey("missing")).thenReturn(Optional.empty());

        PaginationRequest request = new PaginationRequest(0, 10, "name", "ASC");
        assertThrows(OfficeNotFoundException.class,
                () -> officeReadService.getOffices("missing", null, null, request));
    }

    // --- getOfficesByCodePrefix ---

    @Test
    void getOfficesByCodePrefix_returnsMatches() {
        Office o1 = buildOffice(1L, "A", "key-a", "001.001", 1L);
        Office o2 = buildOffice(2L, "B", "key-b", "001.002", 1L);
        when(officeRepositoryWrapper.findAllByCodePrefix("001")).thenReturn(List.of(o1, o2));

        List<OfficeResponse> result = officeReadService.getOfficesByCodePrefix("001");

        assertEquals(2, result.size());
    }

    // --- getOfficeTree (no search) ---

    @Test
    void getOfficeTree_noSearchNoParent_returnsRoots() {
        Office root1 = buildOffice(1L, "Root1", "root1", "001", null);
        Office root2 = buildOffice(2L, "Root2", "root2", "002", null);
        when(officeRepositoryWrapper.findAllByParentId(null)).thenReturn(List.of(root1, root2));
        when(officeRepositoryWrapper.countChildrenByParentIds(List.of(1L, 2L)))
                .thenReturn(Map.of(1L, 3, 2L, 0));

        List<OfficeTreeNodeResponse> result = officeReadService.getOfficeTree(null, null);

        assertEquals(2, result.size());
        assertEquals("Root1", result.get(0).getName());
        assertEquals("Root2", result.get(1).getName());
        assertNull(result.get(0).getPathNames());
    }

    @Test
    void getOfficeTree_noSearchWithParent_returnsChildren() {
        Office parent = buildOffice(1L, "Parent", "parent-key", "001", null);
        when(officeRepository.findByKey("parent-key")).thenReturn(Optional.of(parent));

        Office child = buildOffice(2L, "Child", "child-key", "001.001", 1L);
        when(officeRepositoryWrapper.findAllByParentId(1L)).thenReturn(List.of(child));
        when(officeRepositoryWrapper.countChildrenByParentIds(List.of(2L)))
                .thenReturn(Map.of(2L, 0));

        List<OfficeTreeNodeResponse> result = officeReadService.getOfficeTree("parent-key", null);

        assertEquals(1, result.size());
        assertEquals("Child", result.get(0).getName());
    }

    @Test
    void getOfficeTree_noSearchParentNotFound_throws() {
        when(officeRepository.findByKey("missing")).thenReturn(Optional.empty());

        assertThrows(OfficeNotFoundException.class,
                () -> officeReadService.getOfficeTree("missing", null));
    }

    @Test
    void getOfficeTree_noSearch_childCountsPopulated() {
        Office root = buildOffice(1L, "Root", "root-key", "001", null);
        when(officeRepositoryWrapper.findAllByParentId(null)).thenReturn(List.of(root));
        when(officeRepositoryWrapper.countChildrenByParentIds(List.of(1L)))
                .thenReturn(Map.of(1L, 5));

        List<OfficeTreeNodeResponse> result = officeReadService.getOfficeTree(null, null);

        assertEquals(5, result.get(0).getChildCount());
    }

    // --- getOfficeTree (with search) ---

    @Test
    void getOfficeTree_withSearch_emptyMatches_returnsEmpty() {
        when(officeRepositoryWrapper.findAllByNameContaining("xyz")).thenReturn(List.of());

        List<OfficeTreeNodeResponse> result = officeReadService.getOfficeTree(null, "xyz");

        assertTrue(result.isEmpty());
    }

    @Test
    void getOfficeTree_withSearch_buildsAncestorPaths() {
        // grandchild(id=3, parentId=2) → child(id=2, parentId=1) → root(id=1, parentId=null)
        Office grandchild = buildOffice(3L, "GrandChild", "gc-key", "001.001.001", 2L);
        Office child = buildOffice(2L, "Child", "child-key", "001.001", 1L);
        Office root = buildOffice(1L, "Root", "root-key", "001", null);

        when(officeRepositoryWrapper.findAllByNameContaining("Grand")).thenReturn(List.of(grandchild));
        lenient().when(officeRepository.findAllById(any())).thenAnswer(inv -> {
            Iterable<Long> ids = inv.getArgument(0);
            java.util.Set<Long> idSet = new java.util.HashSet<>();
            ids.forEach(idSet::add);
            if (idSet.contains(2L)) {
                return List.of(child);
            }
            if (idSet.contains(1L)) {
                return List.of(root);
            }
            return List.of();
        });
        when(officeRepositoryWrapper.countChildrenByParentIds(List.of(3L)))
                .thenReturn(Map.of(3L, 0));

        List<OfficeTreeNodeResponse> result = officeReadService.getOfficeTree(null, "Grand");

        assertEquals(1, result.size());
        OfficeTreeNodeResponse node = result.get(0);
        assertEquals(List.of("Root", "Child", "GrandChild"), node.getPathNames());
        assertEquals(List.of("root-key", "child-key", "gc-key"), node.getPathKeys());
        assertEquals(0, node.getChildCount());
    }

    @Test
    void getOfficeTree_withSearch_rootMatch_singlePathEntry() {
        Office root = buildOffice(1L, "Root", "root-key", "001", null);
        when(officeRepositoryWrapper.findAllByNameContaining("Root")).thenReturn(List.of(root));
        when(officeRepositoryWrapper.countChildrenByParentIds(List.of(1L)))
                .thenReturn(Map.of(1L, 2));

        List<OfficeTreeNodeResponse> result = officeReadService.getOfficeTree(null, "Root");

        assertEquals(1, result.size());
        assertEquals(List.of("Root"), result.get(0).getPathNames());
        assertEquals(List.of("root-key"), result.get(0).getPathKeys());
        assertEquals(2, result.get(0).getChildCount());
    }

    @Test
    void getOfficeTree_withSearch_childCountsPopulated() {
        Office office = buildOffice(1L, "Office", "office-key", "001", null);
        when(officeRepositoryWrapper.findAllByNameContaining("Office")).thenReturn(List.of(office));
        when(officeRepositoryWrapper.countChildrenByParentIds(List.of(1L)))
                .thenReturn(Map.of(1L, 7));

        List<OfficeTreeNodeResponse> result = officeReadService.getOfficeTree(null, "Office");

        assertEquals(7, result.get(0).getChildCount());
    }

    @Test
    void getOfficeTree_withSearch_ancestorsInMatches_noExtraLoading() {
        // Both parent and child are in the search results
        Office parent = buildOffice(1L, "Parent Office", "parent-key", "001", null);
        Office child = buildOffice(2L, "Child Office", "child-key", "001.001", 1L);

        when(officeRepositoryWrapper.findAllByNameContaining("Office")).thenReturn(List.of(parent, child));
        when(officeRepositoryWrapper.countChildrenByParentIds(List.of(1L, 2L)))
                .thenReturn(Map.of(1L, 1, 2L, 0));

        List<OfficeTreeNodeResponse> result = officeReadService.getOfficeTree(null, "Office");

        assertEquals(2, result.size());
        // parent is already in matches, so findAllById should never be called
        verify(officeRepository, never()).findAllById(anyCollection());
        // Verify path for parent (root, so single entry)
        assertEquals(List.of("Parent Office"), result.get(0).getPathNames());
        // Verify path for child (parent → child)
        assertEquals(List.of("Parent Office", "Child Office"), result.get(1).getPathNames());
    }

    @Test
    void getOffices_blankParentKey_passesNullPrefix() {
        PaginationInfo pagination = new PaginationInfo(0, 10, 0L, 0, 0, false, false);
        when(officeRepositoryWrapper.findOffices(isNull(), isNull(), isNull(), any(PaginationRequest.class)))
                .thenReturn(new PaginatedResponse<>(List.of(), pagination));

        officeReadService.getOffices("   ", null, null, new PaginationRequest(0, 10, "name", "ASC"));

        verify(officeRepository, never()).findByKey(anyString());
        verify(officeRepositoryWrapper).findOffices(isNull(), isNull(), isNull(), any(PaginationRequest.class));
    }

    @Test
    void getOffices_forwardsNameQueryAndActiveOnly() {
        PaginationInfo pagination = new PaginationInfo(0, 10, 0L, 0, 0, false, false);
        when(officeRepositoryWrapper.findOffices(isNull(), eq("hq"), eq(Boolean.TRUE), any(PaginationRequest.class)))
                .thenReturn(new PaginatedResponse<>(List.of(), pagination));

        officeReadService.getOffices(null, "hq", true, new PaginationRequest(0, 10, "name", "ASC"));

        verify(officeRepositoryWrapper).findOffices(isNull(), eq("hq"), eq(Boolean.TRUE), any(PaginationRequest.class));
    }

    @Test
    void getOfficeTree_blankSearch_usesTreeModeNotSearch() {
        Office root = buildOffice(1L, "Root", "root-key", "001", null);
        when(officeRepositoryWrapper.findAllByParentId(null)).thenReturn(List.of(root));
        when(officeRepositoryWrapper.countChildrenByParentIds(List.of(1L))).thenReturn(Map.of(1L, 0));

        List<OfficeTreeNodeResponse> result = officeReadService.getOfficeTree(null, "   ");

        assertEquals(1, result.size());
        assertNull(result.get(0).getPathNames());
        verify(officeRepositoryWrapper, never()).findAllByNameContaining(anyString());
    }
}
