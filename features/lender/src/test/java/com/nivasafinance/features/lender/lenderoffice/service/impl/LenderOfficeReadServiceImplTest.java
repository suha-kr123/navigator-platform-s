package com.nivasafinance.features.lender.lenderoffice.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeReponseData;
import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeSearchRequest;
import com.nivasafinance.features.lender.lenderoffice.entity.LenderOffice;
import com.nivasafinance.features.lender.lenderoffice.enums.LenderOfficeStatus;
import com.nivasafinance.features.lender.lenderoffice.repository.LenderOfficeRepositoryWrapper;
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
class LenderOfficeReadServiceImplTest {

    @Mock
    private LenderOfficeRepositoryWrapper lenderOfficeRepositoryWrapper;

    @InjectMocks
    private LenderOfficeReadServiceImpl lenderOfficeReadService;

    private UUID officeId;
    private LenderOffice office;

    @BeforeEach
    void setUp() {
        officeId = UUID.randomUUID();
        office = new LenderOffice();
        office.setId(officeId);
        office.setKey("OFF1");
        office.setName("Office");
        office.setLenderKey("L1");
        office.setStatus(LenderOfficeStatus.ACTIVE);
    }

    @Test
    void getByKey_returnsMappedResponse() {
        when(lenderOfficeRepositoryWrapper.findByKeyWithException("OFF1")).thenReturn(office);

        LenderOfficeReponseData result = lenderOfficeReadService.getByKey("OFF1");

        assertEquals(officeId, result.getId());
        assertEquals("OFF1", result.getKey());
    }

    @Test
    void getById_returnsMappedResponse() {
        when(lenderOfficeRepositoryWrapper.findByIdWithException(officeId)).thenReturn(office);

        assertEquals("Office", lenderOfficeReadService.getById(officeId).getName());
    }

    @Test
    void getByLenderKeyAndStatus_mapsList() {
        when(lenderOfficeRepositoryWrapper.findByLenderKeyAndStatus("L1", LenderOfficeStatus.INACTIVE))
                .thenReturn(List.of(office));

        List<LenderOfficeReponseData> result =
                lenderOfficeReadService.getByLenderKeyAndStatus("L1", LenderOfficeStatus.INACTIVE);

        assertEquals(1, result.size());
    }

    @Test
    void getLenderOfficesPaginated_buildsPagination() {
        PaginationRequest p = new PaginationRequest(0, 5, "name", "ASC");
        PageImpl<LenderOffice> page = new PageImpl<>(List.of(office), PageRequest.of(0, 5), 12L);
        when(lenderOfficeRepositoryWrapper.findPage("L1", p, LenderOfficeStatus.ACTIVE)).thenReturn(page);

        PaginatedResponse<LenderOfficeReponseData> result =
                lenderOfficeReadService.getLenderOfficesPaginated("L1", p, LenderOfficeStatus.ACTIVE);

        assertEquals(1, result.getContent().size());
        assertEquals(12L, result.getPagination().getTotalElements());
    }

    @Test
    void getLenderOfficesPaginated_nullStatus_delegatesToWrapper() {
        PaginationRequest p = new PaginationRequest(0, 5, "name", "ASC");
        PageImpl<LenderOffice> page = new PageImpl<>(List.of(office), PageRequest.of(0, 5), 1L);
        when(lenderOfficeRepositoryWrapper.findPage("L1", p, null)).thenReturn(page);

        PaginatedResponse<LenderOfficeReponseData> result =
                lenderOfficeReadService.getLenderOfficesPaginated("L1", p, null);

        assertEquals(1, result.getContent().size());
        verify(lenderOfficeRepositoryWrapper).findPage("L1", p, null);
    }

    @Test
    void searchLenderOffices_blankLenderKey_returnsEmpty() {
        PaginationRequest p = new PaginationRequest(0, 10, "name", "ASC");

        PaginatedResponse<LenderOfficeReponseData> result =
                lenderOfficeReadService.searchLenderOffices("  ", p, new LenderOfficeSearchRequest("abc", null));

        assertTrue(result.getContent().isEmpty());
        assertEquals(0L, result.getPagination().getTotalElements());
        verify(lenderOfficeRepositoryWrapper, never()).searchWithQuery(any(), any(), any(), any());
    }

    @Test
    void searchLenderOffices_validQuery_delegatesToSearch() {
        PaginationRequest p = new PaginationRequest(0, 10, "name", "ASC");
        LenderOfficeSearchRequest req = new LenderOfficeSearchRequest("findme", LenderOfficeStatus.ACTIVE);
        PaginatedResponse<LenderOfficeReponseData> expected = new PaginatedResponse<>(Collections.emptyList(), null);
        when(lenderOfficeRepositoryWrapper.searchWithQuery("L1", p, "findme", LenderOfficeStatus.ACTIVE))
                .thenReturn(expected);

        PaginatedResponse<LenderOfficeReponseData> result =
                lenderOfficeReadService.searchLenderOffices("L1", p, req);

        assertEquals(expected, result);
    }

    @Test
    void searchLenderOffices_shortQuery_returnsEmptyPaginated() {
        PaginationRequest p = new PaginationRequest(0, 10, "name", "ASC");
        LenderOfficeSearchRequest req = new LenderOfficeSearchRequest("no", null);

        PaginatedResponse<LenderOfficeReponseData> result =
                lenderOfficeReadService.searchLenderOffices("L1", p, req);

        assertTrue(result.getContent().isEmpty());
        verify(lenderOfficeRepositoryWrapper, never()).searchWithQuery(any(), any(), any(), any());
    }

    @Test
    void searchLenderOffices_nullRequest_returnsEmptyPaginated() {
        PaginationRequest p = new PaginationRequest(0, 10, "name", "ASC");

        PaginatedResponse<LenderOfficeReponseData> result =
                lenderOfficeReadService.searchLenderOffices("L1", p, null);

        assertTrue(result.getContent().isEmpty());
        verify(lenderOfficeRepositoryWrapper, never()).searchWithQuery(any(), any(), any(), any());
    }

    @Test
    void toResponse_includesNestedAddress() {
        AddressData addr = new AddressData();
        addr.setAddress("Line");
        office.setAddressDetails(LenderOffice.AddressDetails.builder().address(addr).build());
        when(lenderOfficeRepositoryWrapper.findByIdWithException(officeId)).thenReturn(office);

        LenderOfficeReponseData result = lenderOfficeReadService.getById(officeId);

        assertNotNull(result.getAddress());
        assertEquals("Line", result.getAddress().getAddress());
    }

    @Test
    void toResponse_nullOfficeId_throws() {
        LenderOffice bad = new LenderOffice();
        bad.setId(null);
        bad.setKey("K");
        bad.setName("N");
        bad.setLenderKey("L");
        bad.setStatus(LenderOfficeStatus.ACTIVE);
        when(lenderOfficeRepositoryWrapper.findByIdWithException(officeId)).thenReturn(bad);

        assertThrows(IllegalStateException.class, () -> lenderOfficeReadService.getById(officeId));
    }
}
