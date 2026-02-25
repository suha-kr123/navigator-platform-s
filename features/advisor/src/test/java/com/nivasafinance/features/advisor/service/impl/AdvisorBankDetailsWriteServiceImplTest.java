package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.features.advisor.dto.AddBankDetailsRequest;
import com.nivasafinance.features.advisor.dto.BankDetails;
import com.nivasafinance.features.advisor.dto.UpdateBankDetailsRequest;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.enums.BankDetailsStatus;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeMasterService;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.repository.PersonRepositoryWrapper;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvisorBankDetailsWriteServiceImplTest {

    @Mock
    private AdvisorRepositoryWrapper advisorRepositoryWrapper;

    @Mock
    private CodeMasterService codeMasterService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private PersonRepositoryWrapper personRepositoryWrapper;

    @Mock
    private UserReadService userReadService;

    @InjectMocks
    private AdvisorBankDetailsWriteServiceImpl advisorBankDetailsWriteService;

    private static final Long TEST_PERSON_ID = 2L;
    private static final String TEST_ADVISOR_USERNAME = "advisorUser";

    private UUID advisorIdentifier;
    private UUID bankIdentifier;
    private Advisor advisor;

    @BeforeEach
    void setUp() {
        advisorIdentifier = UUID.randomUUID();
        bankIdentifier = UUID.randomUUID();
        advisor = new Advisor();
        advisor.setId(1L);
        advisor.setIdentifier(advisorIdentifier);
        advisor.setUsername(TEST_ADVISOR_USERNAME);
        advisor.setBankDetails(new ArrayList<>());
    }

    @Test
    void addBankDetails_success_returnsBankIdentifier() {
        AddBankDetailsRequest request = new AddBankDetailsRequest();
        request.setNameAsPerPassbook("Name");
        request.setAccountNo("123");
        request.setIsPrimary(false);
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(userReadService.getPersonIdByUsername(TEST_ADVISOR_USERNAME)).thenReturn(TEST_PERSON_ID);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);
        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(new Person());

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");

            UUID result = advisorBankDetailsWriteService.addBankDetails(advisorIdentifier, request);

            assertNotNull(result);
            assertEquals(1, advisor.getBankDetails().size());
            assertEquals(result, advisor.getBankDetails().get(0).getBankIdentifier());
            verify(advisorRepositoryWrapper).saveWithException(advisor);
        }
    }

    @Test
    void addBankDetails_withBankName_validatesAgainstMaster() {
        AddBankDetailsRequest request = new AddBankDetailsRequest();
        request.setBankName("BANK_KEY");
        request.setIsPrimary(false);
        CodeValueResponse cv = new CodeValueResponse();
        cv.setKey("BANK_KEY");
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(userReadService.getPersonIdByUsername(TEST_ADVISOR_USERNAME)).thenReturn(TEST_PERSON_ID);
        when(codeMasterService.getAllCodeValuesByCodeKey(any(), eq(true))).thenReturn(List.of(cv));
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);
        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(new Person());

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");

            UUID result = advisorBankDetailsWriteService.addBankDetails(advisorIdentifier, request);

            assertNotNull(result);
            verify(codeMasterService).getAllCodeValuesByCodeKey(any(), eq(true));
        }
    }

    @Test
    void updateBankDetails_success() {
        BankDetails existing = BankDetails.builder()
                .bankIdentifier(bankIdentifier)
                .isPrimary(false)
                .build();
        advisor.getBankDetails().add(existing);

        UpdateBankDetailsRequest request = new UpdateBankDetailsRequest();
        request.setNameAsPerPassbook("New Name");
        request.setAccountNo("456");
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(userReadService.getPersonIdByUsername(TEST_ADVISOR_USERNAME)).thenReturn(TEST_PERSON_ID);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);
        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(new Person());

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");

            advisorBankDetailsWriteService.updateBankDetails(advisorIdentifier, bankIdentifier, request);

            assertEquals("New Name", existing.getNameAsPerPassbook());
            assertEquals("456", existing.getAccountNo());
            verify(advisorRepositoryWrapper).saveWithException(advisor);
        }
    }

    @Test
    void activateBankDetails_success_returnsBankIdentifier() {
        BankDetails existing = BankDetails.builder()
                .bankIdentifier(bankIdentifier)
                .status(BankDetailsStatus.DEACTIVATED)
                .build();
        advisor.getBankDetails().add(existing);

        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(userReadService.getPersonIdByUsername(TEST_ADVISOR_USERNAME)).thenReturn(TEST_PERSON_ID);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);
        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(new Person());

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");

            UUID result = advisorBankDetailsWriteService.activateBankDetails(advisorIdentifier, bankIdentifier);

            assertEquals(bankIdentifier, result);
            assertEquals(BankDetailsStatus.ACTIVE, existing.getStatus());
        }
    }

    @Test
    void deactivateBankDetails_success_returnsBankIdentifier() {
        BankDetails existing = BankDetails.builder()
                .bankIdentifier(bankIdentifier)
                .status(BankDetailsStatus.ACTIVE)
                .build();
        advisor.getBankDetails().add(existing);

        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(userReadService.getPersonIdByUsername(TEST_ADVISOR_USERNAME)).thenReturn(TEST_PERSON_ID);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);
        when(personRepositoryWrapper.findByIdWithException(TEST_PERSON_ID)).thenReturn(new Person());

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");

            UUID result = advisorBankDetailsWriteService.deactivateBankDetails(advisorIdentifier, bankIdentifier);

            assertEquals(bankIdentifier, result);
            assertEquals(BankDetailsStatus.DEACTIVATED, existing.getStatus());
        }
    }

    @Test
    void getBankDetail_notFound_throwsException() {
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);

        assertThrows(IllegalStateException.class,
                () -> advisorBankDetailsWriteService.updateBankDetails(advisorIdentifier, bankIdentifier, new UpdateBankDetailsRequest()));
    }

    @Test
    void addBankDetails_invalidBankName_throwsException() {
        AddBankDetailsRequest request = new AddBankDetailsRequest();
        request.setBankName("INVALID_BANK");
        request.setIsPrimary(false);
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(codeMasterService.getAllCodeValuesByCodeKey(any(), eq(true))).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class,
                () -> advisorBankDetailsWriteService.addBankDetails(advisorIdentifier, request));
        verify(advisorRepositoryWrapper, never()).saveWithException(any());
    }
}
