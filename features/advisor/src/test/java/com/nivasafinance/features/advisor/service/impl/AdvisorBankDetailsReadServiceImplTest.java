package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.features.advisor.dto.BankDetails;
import com.nivasafinance.features.advisor.dto.BankDetailsResponse;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.enums.BankDetailsStatus;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvisorBankDetailsReadServiceImplTest {

    @Mock
    private AdvisorRepositoryWrapper advisorRepositoryWrapper;

    @InjectMocks
    private AdvisorBankDetailsReadServiceImpl advisorBankDetailsReadService;

    private UUID advisorIdentifier;
    private Advisor advisor;

    @BeforeEach
    void setUp() {
        advisorIdentifier = UUID.randomUUID();
        advisor = new Advisor();
        advisor.setId(1L);
        advisor.setIdentifier(advisorIdentifier);
    }

    @Test
    void getAllBankDetails_nullBankDetails_returnsEmptyList() {
        advisor.setBankDetails(null);
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);

        List<BankDetailsResponse> result = advisorBankDetailsReadService.getAllBankDetails(advisorIdentifier);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllBankDetails_withBankDetails_returnsMappedList() {
        UUID bankId = UUID.randomUUID();
        BankDetails bankDetail = BankDetails.builder()
                .bankIdentifier(bankId)
                .isPrimary(true)
                .nameAsPerPassbook("Name")
                .accountNo("123")
                .bankName("BANK")
                .ifscCode("IFSC")
                .status(BankDetailsStatus.ACTIVE)
                .build();
        advisor.setBankDetails(List.of(bankDetail));
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);

        List<BankDetailsResponse> result = advisorBankDetailsReadService.getAllBankDetails(advisorIdentifier);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(bankId, result.get(0).getBankIdentifier());
        assertTrue(result.get(0).getIsPrimary());
        assertEquals("Name", result.get(0).getNameAsPerPassbook());
    }

    @Test
    void getAllBankDetails_emptyBankDetailsList_returnsEmptyList() {
        advisor.setBankDetails(List.of());
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);

        List<BankDetailsResponse> result = advisorBankDetailsReadService.getAllBankDetails(advisorIdentifier);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
