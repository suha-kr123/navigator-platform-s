package com.nivasafinance.features.lead.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lead.dto.LeadDashboardFilters;
import com.nivasafinance.features.lead.dto.LeadDashboardResponse;
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService;
import com.nivasafinance.features.offices.dto.OfficeResponse;
import com.nivasafinance.features.offices.service.OfficeReadService;
import com.nivasafinance.features.staff.dto.StaffResponse;
import com.nivasafinance.features.staff.service.StaffReadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class LeadDashboardWrapperTest {

    private static final String OFFICE_KEY = "office-key-1";
    private static final String OFFICE_CODE = "001";

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private CodeValueMasterService codeValueMasterService;

    @Mock
    private StaffReadService staffReadService;

    @Mock
    private OfficeReadService officeReadService;

    @Captor
    private ArgumentCaptor<String> sqlCaptor;

    @Captor
    private ArgumentCaptor<Object[]> objectArrayCaptor;

    private LeadDashboardWrapper leadDashboardWrapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        leadDashboardWrapper = new LeadDashboardWrapper(
                jdbcTemplate,
                codeValueMasterService,
                staffReadService,
                officeReadService,
                objectMapper);
        when(staffReadService.getCurrentStaff()).thenReturn(StaffResponse.builder().officeKey(OFFICE_KEY).build());
        when(officeReadService.getOfficeByKey(OFFICE_KEY))
                .thenReturn(new OfficeResponse(null, null, OFFICE_KEY, OFFICE_CODE, null, null, null));
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(Object[].class))).thenReturn(0L);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Object[].class)))
                .thenReturn(Collections.<LeadDashboardResponse>emptyList());
    }

    private static PaginationRequest page(int offset, int limit) {
        return new PaginationRequest(offset, limit, "leadCreatedAt", "ASC");
    }

    @Test
    void findLeadDashboard_withPartnersFilter_usesLenderKeyInExistsClause() {
        LeadDashboardFilters filters = LeadDashboardFilters.builder()
                .partners(List.of("  VERITAS_HL  "))
                .build();

        PaginatedResponse<LeadDashboardResponse> result = leadDashboardWrapper.findLeadDashboard(page(0, 15), filters);

        assertEquals(0, result.getContent().size(), "Result content should be empty for the mocked empty response");

        verify(jdbcTemplate).queryForObject(sqlCaptor.capture(), eq(Long.class), objectArrayCaptor.capture());
        assertTrue(sqlCaptor.getValue().contains("ln_filt.key IN ("), "count SQL should filter partners by n_lender.key");
        assertTrue(sqlCaptor.getValue().contains("ll_filt.status IN ('SELECTED', 'SUBMITTED')"));
        assertArrayEquals(new Object[] { OFFICE_CODE + "%", "ACTIVE", "VERITAS_HL" }, objectArrayCaptor.getValue(), "Bound parameters should match office, status, and partner filter");

        verify(jdbcTemplate).query(sqlCaptor.capture(), any(RowMapper.class), objectArrayCaptor.capture());
        assertTrue(sqlCaptor.getAllValues().get(1).contains("ln_filt.key IN ("), "data SQL should filter partners by n_lender.key");
        assertArrayEquals(new Object[] { OFFICE_CODE + "%", "ACTIVE", "VERITAS_HL", 15, 0 }, objectArrayCaptor.getValue(), "Data SQL parameters should include pagination offsets");
    }

    @Test
    void findLeadDashboard_withMultiplePartners_bindsAllKeys() {
        LeadDashboardFilters filters = LeadDashboardFilters.builder()
                .partners(List.of("VERITAS_HL", "OTHER_LENDER"))
                .build();

        leadDashboardWrapper.findLeadDashboard(page(0, 10), filters);

        verify(jdbcTemplate).queryForObject(anyString(), eq(Long.class), objectArrayCaptor.capture());
        assertArrayEquals(
                new Object[] { OFFICE_CODE + "%", "ACTIVE", "VERITAS_HL", "OTHER_LENDER" },
                objectArrayCaptor.getValue(),
                "Bound parameters should include all partner keys");
    }

    @Test
    void findLeadDashboard_withNoPartnersFilter_doesNotBindPartnerKeys() {
        LeadDashboardFilters filters = LeadDashboardFilters.builder().partners(null).build();

        leadDashboardWrapper.findLeadDashboard(page(0, 5), filters);

        verify(jdbcTemplate).queryForObject(anyString(), eq(Long.class), objectArrayCaptor.capture());
        assertArrayEquals(new Object[] { OFFICE_CODE + "%", "ACTIVE" }, objectArrayCaptor.getValue(), "Bound parameters should only include office and status when no partners are filtered");

        verify(jdbcTemplate).query(anyString(), any(RowMapper.class), objectArrayCaptor.capture());
        assertArrayEquals(new Object[] { OFFICE_CODE + "%", "ACTIVE", 5, 0 }, objectArrayCaptor.getValue(), "Data SQL parameters should only include office, status, and pagination");
    }

    @Test
    void findLeadDashboard_withOnlyBlankPartners_skipsPartnerPredicate() {
        LeadDashboardFilters filters =
                LeadDashboardFilters.builder().partners(Arrays.asList(" ", "", null)).build();

        leadDashboardWrapper.findLeadDashboard(page(0, 5), filters);

        verify(jdbcTemplate).queryForObject(anyString(), eq(Long.class), objectArrayCaptor.capture());
        assertArrayEquals(new Object[] { OFFICE_CODE + "%", "ACTIVE" }, objectArrayCaptor.getValue(), "Bound parameters should only include office and status when partner filter is blank");
    }
}
