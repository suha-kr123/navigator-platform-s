package com.nivasafinance.features.lender.lender.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lender.lender.dto.LenderResponseData;
import com.nivasafinance.features.lender.lender.dto.LenderSearchRequest;
import com.nivasafinance.features.lender.lender.entity.Lender;
import com.nivasafinance.features.lender.lender.enums.LenderStatus;
import com.nivasafinance.features.lender.lender.repository.LenderRepositoryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LenderReadServiceImplTest {

    @Mock
    private LenderRepositoryWrapper lenderRepositoryWrapper;

    @InjectMocks
    private LenderReadServiceImpl lenderReadService;

    private UUID lenderId;
    private Lender lender;

    @BeforeEach
    void setUp() {
        lenderId = UUID.randomUUID();
        lender = new Lender();
        lender.setId(lenderId);
        lender.setKey("KEY1");
        lender.setName("Test Lender");
        lender.setStatus(LenderStatus.ACTIVE);
    }

    @Test
    void getById_returnsMappedResponse() {
        when(lenderRepositoryWrapper.findByIdWithException(lenderId)).thenReturn(lender);

        LenderResponseData result = lenderReadService.getById(lenderId);

        assertEquals(lenderId, result.getId());
        assertEquals("Test Lender", result.getName());
        assertEquals("KEY1", result.getKey());
        assertEquals(LenderStatus.ACTIVE, result.getStatus());
    }

    @Test
    void getByKey_returnsMappedResponse() {
        when(lenderRepositoryWrapper.findByKeyWithException("KEY1")).thenReturn(lender);

        LenderResponseData result = lenderReadService.getByKey("KEY1");

        assertEquals("KEY1", result.getKey());
    }

    @Test
    void getAllByStatus_mapsList() {
        when(lenderRepositoryWrapper.findAllByStatus(LenderStatus.INACTIVE)).thenReturn(List.of(lender));

        List<LenderResponseData> result = lenderReadService.getAllByStatus(LenderStatus.INACTIVE);

        assertEquals(1, result.size());
        assertEquals(lenderId, result.get(0).getId());
    }

    @Test
    void getLendersPaginated_buildsPagination() {
        PaginationRequest p = new PaginationRequest(0, 10, "name", "ASC");
        PageImpl<Lender> page = new PageImpl<>(List.of(lender), PageRequest.of(0, 10), 25L);
        when(lenderRepositoryWrapper.findPage(p, LenderStatus.ACTIVE)).thenReturn(page);

        PaginatedResponse<LenderResponseData> result = lenderReadService.getLendersPaginated(p, LenderStatus.ACTIVE);

        assertEquals(1, result.getContent().size());
        assertNotNull(result.getPagination());
        assertEquals(25L, result.getPagination().getTotalElements());
        assertTrue(result.getPagination().isHasNext());
        assertFalse(result.getPagination().isHasPrevious());
    }

    @Test
    void getLendersPaginated_offsetSecondPage_hasPrevious() {
        PaginationRequest p = new PaginationRequest(10, 10, "name", "ASC");
        PageImpl<Lender> page = new PageImpl<>(List.of(lender), PageRequest.of(1, 10), 25L);
        when(lenderRepositoryWrapper.findPage(p, null)).thenReturn(page);

        PaginatedResponse<LenderResponseData> result = lenderReadService.getLendersPaginated(p, null);

        assertTrue(result.getPagination().isHasPrevious());
    }

    @Test
    void searchLenders_validQuery_delegatesToSearch() {
        PaginationRequest p = new PaginationRequest(0, 20, "name", "ASC");
        LenderSearchRequest req = new LenderSearchRequest("abc", LenderStatus.ACTIVE);
        PaginatedResponse<LenderResponseData> expected = new PaginatedResponse<>(Collections.emptyList(), null);
        when(lenderRepositoryWrapper.searchWithQuery(p, "abc", LenderStatus.ACTIVE)).thenReturn(expected);

        PaginatedResponse<LenderResponseData> result = lenderReadService.searchLenders(p, req);

        assertEquals(expected, result);
        verify(lenderRepositoryWrapper).searchWithQuery(p, "abc", LenderStatus.ACTIVE);
        verify(lenderRepositoryWrapper, never()).findPage(any(), any());
    }

    @Test
    void searchLenders_shortQuery_fallsBackToPaginated() {
        PaginationRequest p = new PaginationRequest(0, 20, "name", "ASC");
        LenderSearchRequest req = new LenderSearchRequest("ab", LenderStatus.ACTIVE);
        PageImpl<Lender> page = new PageImpl<>(List.of(lender), PageRequest.of(0, 20), 1L);
        when(lenderRepositoryWrapper.findPage(p, LenderStatus.ACTIVE)).thenReturn(page);

        PaginatedResponse<LenderResponseData> result = lenderReadService.searchLenders(p, req);

        assertEquals(1, result.getContent().size());
        verify(lenderRepositoryWrapper, never()).searchWithQuery(any(), any(), any());
    }

    @Test
    void searchLenders_nullRequest_usesPaginatedWithNullStatus() {
        PaginationRequest p = new PaginationRequest(0, 20, "name", "ASC");
        PageImpl<Lender> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0L);
        when(lenderRepositoryWrapper.findPage(p, null)).thenReturn(page);

        PaginatedResponse<LenderResponseData> result = lenderReadService.searchLenders(p, null);

        assertNotNull(result);
        verify(lenderRepositoryWrapper).findPage(p, null);
    }

    @Test
    void toResponse_nullLenderId_throwsIllegalState() {
        Lender bad = new Lender();
        bad.setId(null);
        bad.setKey("K");
        bad.setName("N");
        bad.setStatus(LenderStatus.ACTIVE);
        when(lenderRepositoryWrapper.findByIdWithException(lenderId)).thenReturn(bad);

        assertThrows(IllegalStateException.class, () -> lenderReadService.getById(lenderId));
    }
}
